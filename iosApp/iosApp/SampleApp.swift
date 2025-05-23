import SwiftUI
import shared

@main
struct SampleApp: App {
	var body: some Scene {
		WindowGroup {
			SampleView()
		}
	}
}

struct SampleView: View {

    @State var text = ""
    var body: some View {
        VStack {
            Text(text)
            Button("LOAD") {
                loadResponsesFlow()
            }
        }
    }

    let api: SampleKtorApiDataSource

    init() {
        api = SampleKtorApiDataSource(
            platform: SampleIosPlatform(),
            logger: SampleIosLogger()
        )
    }

    func loadResponsesFlow() {
        self.text = "LOADING"
        FlowWrapper<SampleIosResult>(
            flow: api.getRepositoriesFlow()
        )
            .subscribe { item in
                self.text += "\n\nloadResponsesFlow onEach \(item)"
                print(self.text)
            } onComplete: {
                self.text += "\n\nloadResponsesFlow onComplete"
                print(self.text)
            } onThrow: { error in
                self.text += "\n\nloadResponsesFlow onThrow \(error)"
                print(self.text)
            }
    }
}