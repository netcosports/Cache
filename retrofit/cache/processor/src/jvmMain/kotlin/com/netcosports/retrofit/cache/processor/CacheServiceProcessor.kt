package com.netcosports.retrofit.cache.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.netcosports.cache.core.Loader
import com.netcosports.okhttp.cache.data.OkHttpCacheInterceptor
import com.netcosports.retrofit.cache.data.CacheService
import io.reactivex.Single
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.jetbrains.annotations.Nullable
import retrofit2.Retrofit
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import kotlin.coroutines.Continuation
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

class CacheServiceProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(CacheService::class.java.name)
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(CacheService::class.java)
            .forEach { element ->
                if (!generateService(element)) {
                    return false
                }
            }

        return true
    }

    private fun generateService(element: Element): Boolean {
        return generateFile(element).save()
    }

    private fun generateFile(element: Element): FileSpec {
        val serviceName = element.simpleName.toString()
        val generatedServiceName = "$serviceName$CLASS_SUFFIX"
        val servicePackage = processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()

        return FileSpec.builder(servicePackage, generatedServiceName)
            .addType(
                generateClass(serviceName, servicePackage, generatedServiceName)
                    .addFunctions(generateFuns(element))
                    .build()
            )
            .build()
    }

    private fun generateClass(serviceName: String, servicePackage: String, generatedServiceName: String): TypeSpec.Builder {
        val serviceClass = ClassName(servicePackage, serviceName)
        return TypeSpec.classBuilder(generatedServiceName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(CACHE_SERVICE_FIELD, serviceClass)
                    .addParameter(API_SERVICE_FIELD, serviceClass)
                    .build()
            )
            .addProperty(
                PropertySpec.builder(CACHE_SERVICE_FIELD, serviceClass)
                    .initializer(CACHE_SERVICE_FIELD)
                    .build()
            )
            .addProperty(
                PropertySpec.builder(API_SERVICE_FIELD, serviceClass)
                    .initializer(API_SERVICE_FIELD)
                    .build()
            )
            .addType(generateCompanion(serviceClass, servicePackage, generatedServiceName))
    }

    private fun generateFuns(element: Element): List<FunSpec> {
        return element.enclosedElements
            .filter { it.kind == ElementKind.METHOD }
            .filterIsInstance<ExecutableElement>()
            .map { executableElement -> generateFun(executableElement) }
    }

    private fun generateFun(executableElement: ExecutableElement): FunSpec {
        val funName = executableElement.simpleName.toString()
        var _continuation: ParameterizedTypeName? = null
        val filteredParams = executableElement.parameters.mapNotNull { parameter ->
            val parameterTypeName = parameter.asType().asCorrectTypeName(parameter)
            val parameterizedParameterTypeName = parameterTypeName as? ParameterizedTypeName
            val isContinuation = parameterizedParameterTypeName != null
                && parameterTypeName.rawType.simpleName == continuationClassName
            if (isContinuation) {
                _continuation = parameterizedParameterTypeName
                null
            } else {
                ParameterSpec.builder(parameter.simpleName.toString(), parameterTypeName)
                    .build()
            }
        }
        val continuation = _continuation
        val funParams = filteredParams.joinToString(", ") { it.name }
        val funReturnType = if (continuation == null) {
            executableElement.returnType.asCorrectTypeName(executableElement)
        } else {
            val coroutineReturnType = continuation.typeArguments.first()

            val fixedParameterTypeName = if (coroutineReturnType is WildcardTypeName) {
                coroutineReturnType.removeWildcards() //remove unexpected in/out from list/maps
            } else {
                coroutineReturnType
            }

            LambdaTypeName.get(returnType = fixedParameterTypeName).copy(suspending = true)
        }

        return FunSpec.builder(executableElement.simpleName.toString())
            .addParameters(filteredParams)
            .generateCodeAndReturnType(
                isSuspendedFun = continuation != null,
                funName = funName,
                funParams = funParams,
                funReturnType = funReturnType
            )
            .build()
    }

    private val singleClassName = Single::class.java.simpleName
    private val continuationClassName = Continuation::class.java.simpleName

    private fun FunSpec.Builder.generateCodeAndReturnType(
        isSuspendedFun: Boolean,
        funName: String,
        funParams: String,
        funReturnType: TypeName
    ): FunSpec.Builder {
        val useCustomCoroutineBlock: Boolean
        val useCustomRxBlock: Boolean
        val newFunReturnType: TypeName
        when {
            isSuspendedFun -> {
                useCustomCoroutineBlock = true
                useCustomRxBlock = false
                newFunReturnType = Loader::class.asClassName()
                    .parameterizedBy(funReturnType)
            }
            funReturnType is ParameterizedTypeName && funReturnType.rawType.simpleName == singleClassName -> {
                useCustomCoroutineBlock = false
                useCustomRxBlock = true
                newFunReturnType = Loader::class.asClassName()
                    .parameterizedBy(funReturnType)
            }
            else -> {
                useCustomCoroutineBlock = false
                useCustomRxBlock = false
                newFunReturnType = funReturnType
            }
        }

        val code = when {
            useCustomCoroutineBlock -> {
                CodeBlock.builder()
                    .addStatement(
                        """
                    |return %T(
                    |    suspend { $CACHE_SERVICE_FIELD.$funName($funParams) },
                    |    suspend { $API_SERVICE_FIELD.$funName($funParams) }
                    |)
                    """.trimMargin(), Loader::class
                    ).build()
            }
            useCustomRxBlock -> {
                CodeBlock.builder()
                    .addStatement(
                        """
                    |return %T(
                    |    $CACHE_SERVICE_FIELD.$funName($funParams),
                    |    $API_SERVICE_FIELD.$funName($funParams)
                    |)
                    """.trimMargin(), Loader::class
                    ).build()
            }
            else -> {
                CodeBlock.builder()
                    .addStatement("return $API_SERVICE_FIELD.$funName($funParams)")
                    .build()
            }
        }
        this.addCode(code)
        this.returns(newFunReturnType)
        return this
    }

    private fun generateCompanion(serviceClass: ClassName, servicePackage: String, generatedServiceName: String): TypeSpec {
        return TypeSpec.companionObjectBuilder()
            .addFunction(generateCompanionFun(serviceClass, servicePackage, generatedServiceName))
            .build()
    }

    private fun generateCompanionFun(
        serviceClass: ClassName,
        servicePackage: String,
        generatedServiceName: String
    ): FunSpec {
        val generatedServiceClass = ClassName(servicePackage, generatedServiceName)
        return FunSpec.builder(COMPANION_FUN_NAME)
            .addParameter(COMPANION_FUN_PARAM_RETROFIT, Retrofit.Builder::class.java)
            .addParameter(COMPANION_FUN_PARAM_OKHTTP, OkHttpClient.Builder::class.java)
            .addParameter(COMPANION_FUN_PARAM_CACHE, Cache::class.java)
            .addParameter(
                ParameterSpec.builder(COMPANION_FUN_PARAM_MAX_STALE, Long::class)
                    .defaultValue(
                        CodeBlock.of(
                            "%M",
                            MemberName(
                                OkHttpCacheInterceptor.Companion::class.java.canonicalName,
                                "DEFAULT_MAX_STALE"
                            )
                        )
                    )
                    .build()
            )
            .addCode(
                CodeBlock.builder()
                    .addStatement(
                        """
                        |return %M<${serviceClass.simpleName}, $generatedServiceName>(
                        |   $COMPANION_FUN_PARAM_RETROFIT, $COMPANION_FUN_PARAM_OKHTTP, $COMPANION_FUN_PARAM_CACHE, $COMPANION_FUN_PARAM_MAX_STALE
                        |) { $COMPANION_FUN_FIELD_CACHE_SERVICE, $COMPANION_FUN_FIELD_API_SERVICE ->
                        |   $generatedServiceName($COMPANION_FUN_FIELD_CACHE_SERVICE, $COMPANION_FUN_FIELD_API_SERVICE)
                        |}""".trimMargin(),
                        MemberName("com.netcosports.retrofit.cache.data", "createServiceWrapper")
                    )
                    .build()
            )
            .returns(generatedServiceClass)
            .build()
    }

    private fun FileSpec.save(): Boolean {
        writeTo(processingEnv.filer)
        return true
    }

    private fun TypeMirror.asCorrectTypeName(element: Element): TypeName {
        val annotation = element.getAnnotation(Nullable::class.java)
        val typeName = asTypeName().javaToKotlinType()
        return if (annotation == null) typeName else typeName.copy(nullable = true)
    }

    private fun TypeName.javaToKotlinType(): TypeName {
        return when (this) {
            is ParameterizedTypeName -> {
                (rawType.javaToKotlinType() as ClassName).parameterizedBy(
                    *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
                )
            }
            is WildcardTypeName -> {
                if (inTypes.isNotEmpty()) {
                    WildcardTypeName.consumerOf(inTypes[0].javaToKotlinType())
                } else {
                    WildcardTypeName.producerOf(outTypes[0].javaToKotlinType())
                }
            }
            else -> {
                val className = JavaToKotlinClassMap.INSTANCE
                    .mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
                if (className == null) {
                    this
                } else {
                    ClassName.bestGuess(className)
                }
            }
        }.copy(nullable = isNullable)
    }

    private fun TypeName.removeWildcards(): TypeName {
        return when (this) {
            is ParameterizedTypeName -> {
                rawType.parameterizedBy(
                    *typeArguments.map { it.removeWildcards() }.toTypedArray()
                )
            }
            is WildcardTypeName -> {
                if (inTypes.isNotEmpty()) {
                    inTypes[0].removeWildcards()
                } else {
                    outTypes[0].removeWildcards()
                }
            }
            else -> this
        }.copy(nullable = isNullable)
    }

    private fun errorMessage(message: String) {
        processingEnv.messager.printMessage(javax.tools.Diagnostic.Kind.ERROR, message)
    }

    private fun noteMessage(message: String) {
        processingEnv.messager.printMessage(javax.tools.Diagnostic.Kind.NOTE, message)
    }

    private fun warningMessage(message: String) {
        processingEnv.messager.printMessage(javax.tools.Diagnostic.Kind.WARNING, message)
    }

    companion object {
        private const val CLASS_SUFFIX = "Wrapper"

        private const val COMPANION_FUN_NAME = "create"
        private const val COMPANION_FUN_PARAM_RETROFIT = "retrofitBuilder"
        private const val COMPANION_FUN_PARAM_OKHTTP = "okHttpBuilder"
        private const val COMPANION_FUN_PARAM_MAX_STALE = "maxStale"
        private const val COMPANION_FUN_PARAM_CACHE = "cache"
        private const val COMPANION_FUN_FIELD_CACHE_SERVICE = "cacheService"
        private const val COMPANION_FUN_FIELD_API_SERVICE = "apiService"

        private const val CACHE_SERVICE_FIELD = "cacheService"
        private const val API_SERVICE_FIELD = "apiService"
    }
}