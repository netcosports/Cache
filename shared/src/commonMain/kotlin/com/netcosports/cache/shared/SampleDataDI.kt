package com.netcosports.cache.shared

class SampleDataDI(
    platform: SamplePlatform,
    logger: SampleLogger
) {

    val commonDataDI: SampleCommonDataDI = SampleCommonDataDI(
        platform = platform,
        logger = logger
    )
}