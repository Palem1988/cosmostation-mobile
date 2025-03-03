//
//  AddAddressViewController.swift
//  Cosmostation
//
//  Created by yongjoo on 27/03/2019.
//  Copyright © 2019 wannabit. All rights reserved.
//

import UIKit

class AddAddressViewController: BaseViewController {

    @IBOutlet weak var addAddressMsgLabel: UILabel!
    @IBOutlet weak var addAddressInputText: AddressInputTextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(self.dismissKeyboard (_:)))
        self.view.addGestureRecognizer(tapGesture)
    }
    
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.setNavigationBarHidden(false, animated: false)
        self.navigationController?.navigationBar.topItem?.title = NSLocalizedString("title_watch_wallet", comment: "")
        self.navigationController?.navigationBar.setBackgroundImage(UIImage(), for: UIBarMetrics.default)
        self.navigationController?.navigationBar.shadowImage = UIImage()
    }
    
    @objc func dismissKeyboard (_ sender: UITapGestureRecognizer) {
        self.view.endEditing(true)
    }
    
    @IBAction func onClickPaste(_ sender: Any) {
        if let myString = UIPasteboard.general.string {
            self.addAddressInputText.text = myString
        } else {
            self.onShowToast(NSLocalizedString("error_no_clipboard", comment: ""))
        }
    }
    
    @IBAction func onClickCancel(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func onClickNext(_ sender: Any) {
        let userInput = self.addAddressInputText.text?.trimmingCharacters(in: .whitespaces) ?? ""
        if (userInput.starts(with: "cosmos")) {
            if (userInput.starts(with: "cosmosvaloper")) {
                self.onShowToast(NSLocalizedString("error_invalid_address_or_pubkey", comment: ""))
                self.addAddressInputText.text = ""
                return;
            } else if (WKey.isValidateBech32(userInput)) {
                self.onGenWatchAccount(ChainType.SUPPORT_CHAIN_COSMOS_MAIN, userInput)
                return;
            } else {
                self.onShowToast(NSLocalizedString("error_invalid_address_or_pubkey", comment: ""))
                self.addAddressInputText.text = ""
                return;
            }
            
        } else if (userInput.starts(with: "iaa")) {
            if (WKey.isValidateBech32(userInput)) {
                self.onGenWatchAccount(ChainType.SUPPORT_CHAIN_IRIS_MAIN, userInput)
                return;
            } else {
                self.onShowToast(NSLocalizedString("error_invalid_address_or_pubkey", comment: ""))
                self.addAddressInputText.text = ""
                return;
            }
            
        } else {
            self.onShowToast(NSLocalizedString("error_invalid_address_or_pubkey", comment: ""))
            self.addAddressInputText.text = ""
            return;
            
        }
    }
    
    func onGenWatchAccount(_ chain:ChainType, _ address: String) {
        self.showWaittingAlert()
        DispatchQueue.global().async {
            let newAccount = Account.init(isNew: true)
            newAccount.account_address = address
            newAccount.account_base_chain = chain.rawValue
            newAccount.account_has_private = false
            newAccount.account_from_mnemonic = false
            newAccount.account_import_time = Date().millisecondsSince1970
            let insertResult = BaseData.instance.insertAccount(newAccount)
            
            DispatchQueue.main.async(execute: {
                self.hideWaittingAlert()
                if(insertResult > 0) {
                    BaseData.instance.setLastTab(0)
                    BaseData.instance.setRecentAccountId(insertResult)
                    self.onStartMainTab()
                } else {
                    //TODO Error control
                }
            });
        }
    }
}
