//
//  CacheVC.swift
//  iosApp
//
//  Created by Vladimir Garkovich on 6/10/21.
//  Copyright © 2021 orgName. All rights reserved.
//

import SwiftUI
import shared

class SampleCacheVC: UIViewController {

    let api = Sample.data.commonDataDI.provideApiDataSource()

    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
//        self.loadResponseCache()
//        self.loadResponseApi()
        self.loadResponsesFlow()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
    }

    func loadResponseCache() {
        api.getResponseCache { data, error in
            if let error = error {
                print("TEST CACHE loadResponseCache ERROR = \(error)")
            } else {
                print("TEST CACHE loadResponseCache DATA = \(data?.data)")
            }
        }
    }
    
    func loadResponseApi() {
        api.getResponseCache { data, error in
            if let error = error {
                print("TEST CACHE loadResponseApi ERROR = \(error)")
            } else {
                print("TEST CACHE loadResponseApi DATA = \(data?.data)")
            }
        }
    }
    
    func loadResponsesFlow() {
        let job: Kotlinx_coroutines_coreJob = api.getResponsesFlow().observe().subscribe { item in
            print("TEST CACHE loadResponsesSuspended onEach \(item)")
        } onComplete: {
            print("TEST CACHE loadResponsesSuspended onComplete")
        } onThrow: { error in
            print("TEST CACHE loadResponsesSuspended onThrow \(error)")
        }
    }
}

public extension Kotlinx_coroutines_coreFlow {
    func observe() -> FlowWrapper<ResponseWrapper<SampleCacheEntity>> {
        return FlowWrapper(flow: self)
    }
}
