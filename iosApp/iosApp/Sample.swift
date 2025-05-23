import Foundation
import shared

public final class Sample {
    public static let data: SampleDataDI = SampleDataDI(
        platform: SamplePlatform(),
        logger: SampleLogger()
    )
}
