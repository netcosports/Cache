package com.originsdigital.cache.retrofit.processor

import com.originsdigital.cache.core.LoaderArguments
import com.originsdigital.cache.ktx.CoroutineLoader
import com.originsdigital.cache.retrofit.data.CacheService
import com.originsdigital.cache.rx.SingleLoader
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import io.reactivex.Single
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
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

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
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
        val servicePackage =
            processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()

        return FileSpec.Companion.builder(servicePackage, generatedServiceName)
            .addType(
                generateClass(serviceName, servicePackage, generatedServiceName)
                    .addFunctions(generateFuns(element))
                    .build()
            )
            .build()
    }

    private fun generateClass(
        serviceName: String,
        servicePackage: String,
        generatedServiceName: String
    ): TypeSpec.Builder {
        val serviceClass = ClassName(servicePackage, serviceName)
        return TypeSpec.Companion.classBuilder(generatedServiceName)
            .primaryConstructor(
                FunSpec.Companion.constructorBuilder()
                    .addParameter(CACHE_SERVICE_FIELD, serviceClass)
                    .addParameter(API_SERVICE_FIELD, serviceClass)
                    .build()
            )
            .addProperty(
                PropertySpec.Companion.builder(CACHE_SERVICE_FIELD, serviceClass)
                    .initializer(CACHE_SERVICE_FIELD)
                    .build()
            )
            .addProperty(
                PropertySpec.Companion.builder(API_SERVICE_FIELD, serviceClass)
                    .initializer(API_SERVICE_FIELD)
                    .build()
            )
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
                ParameterSpec.Companion.builder(parameter.simpleName.toString(), parameterTypeName)
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

            fixedParameterTypeName
        }

        return FunSpec.Companion.builder(executableElement.simpleName.toString())
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
                newFunReturnType = CoroutineLoader::class.asClassName()
                    .parameterizedBy(funReturnType)
            }

            funReturnType is ParameterizedTypeName && funReturnType.rawType.simpleName == singleClassName -> {
                useCustomCoroutineBlock = false
                useCustomRxBlock = true
                newFunReturnType = SingleLoader::class.asClassName()
                    .parameterizedBy(funReturnType.typeArguments)
            }

            else -> {
                useCustomCoroutineBlock = false
                useCustomRxBlock = false
                newFunReturnType = funReturnType
            }
        }

        val code = when {
            useCustomCoroutineBlock -> {
                CodeBlock.Companion.of(
                    """
                        |return %M { loaderArguments: %T ->
                        |   when (loaderArguments) {
                        |       is LoaderArguments.API -> $API_SERVICE_FIELD.$funName($funParams)
                        |       is LoaderArguments.CACHE -> $CACHE_SERVICE_FIELD.$funName($funParams)
                        |   }
                        |}
                        """.trimMargin(),
                    MemberName("com.originsdigital.cache.ktx", "suspendLoader"),
                    LoaderArguments::class,
                )
            }

            useCustomRxBlock -> {
                CodeBlock.Companion.of(
                    """
                        |return %M { loaderArguments: %T ->
                        |   when (loaderArguments) {
                        |       is LoaderArguments.API -> $API_SERVICE_FIELD.$funName($funParams)
                        |       is LoaderArguments.CACHE -> $CACHE_SERVICE_FIELD.$funName($funParams)
                        |   }
                        |}
                        """.trimMargin(),
                    MemberName("com.originsdigital.cache.rx", "singleLoader"),
                    LoaderArguments::class,
                )
            }

            else -> {
                CodeBlock.Companion.builder()
                    .addStatement("return $API_SERVICE_FIELD.$funName($funParams)")
                    .build()
            }
        }
        this.addCode(code)
        this.returns(newFunReturnType)
        return this
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
                    WildcardTypeName.Companion.consumerOf(inTypes[0].javaToKotlinType())
                } else {
                    WildcardTypeName.Companion.producerOf(outTypes[0].javaToKotlinType())
                }
            }

            else -> {
                val className = JavaToKotlinClassMap.INSTANCE
                    .mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
                if (className == null) {
                    this
                } else {
                    ClassName.Companion.bestGuess(className)
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
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    private fun noteMessage(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, message)
    }

    private fun warningMessage(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, message)
    }

    companion object {
        private const val CLASS_SUFFIX = "Wrapper"

        private const val CACHE_SERVICE_FIELD = "cacheService"
        private const val API_SERVICE_FIELD = "apiService"
    }
}