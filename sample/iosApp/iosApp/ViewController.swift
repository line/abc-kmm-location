//
//  ViewController.swift
//  iosApp
//
//  Created by SJin Han on 2021/05/07.
//

import UIKit
import shared
import CoreLocation

class ViewController: UIViewController {
    
    @IBOutlet weak var locationLabel: UILabel!
    
    deinit {
        ABCLocation.Companion().stopLocationUpdating()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        ABCLocation.Companion().requiredPermission = .authorizedalways
    }
    
    @IBAction func tappedCurrent(_ sender: UIButton) {
        ABCLocation.Companion()
            .onLocationUnavailable(target: "SingleRequest") { [unowned self] in
                print("onLocationUnavailable")
                showPermissionDeniedAlert()
            }
            .onPermissionUpdated(target: <#T##Any#>, block: <#T##(KotlinBoolean) -> Void#>)
            .currentLocation { [unowned self] data in
                print("location coordinates", Date(), data.coordinates)
                locationLabel.text = "Single \(data.coordinates.latitude)\n\(locationLabel.text!)"
            }
    }

    @IBAction func tappedStartForegroundLocation(_ sender: UIButton) {
        ABCLocation.Companion()
            .onLocationUnavailable(target: self) { [unowned self] in
                print("onLocationUnavailable")
                showPermissionDeniedAlert()
            }
            .onLocationUpdated(target: self) { [unowned self] data in
                print("location coordinates", Date(), data.coordinates)
                locationLabel.text = "Continuous \(data.coordinates.latitude)\n\(locationLabel.text!)"
            }
            .
            .startLocationUpdating()
    }

    @IBAction func tappedStopForegroundLocation(_ sender: UIButton) {
        ABCLocation.Companion().removeListeners(target: self)
        locationLabel.text = "Loation"
    }

    private func showPermissionDeniedAlert() {
        let defaultMessage = "Location services are not available.\nTurn on location services in the device's \"Setting > Privacy\"."
        let alert = UIAlertController(title: "", message: defaultMessage, preferredStyle: .alert)
        let cancel = UIAlertAction(title: "Cancel", style: .cancel)
        let landing = UIAlertAction(title: "Go to Setting", style: .default) { _ in
            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
        }
        alert.addAction(cancel)
        alert.addAction(landing)
        present(alert, animated: true)
    }
}
