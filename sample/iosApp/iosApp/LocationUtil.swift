//
//  LocationUtil.swift
//  iosApp
//
//  Created by SJin Han on 2021/05/10.
//

import Foundation
import shared
import CoreLocation

struct LocationUtil {
    
    static func convert(location: CLLocation, heading: CLHeading) -> LocationData {
        /*
         let coordinates = Coordinates(
             latitude: location.coordinate.latitude,
             longitude: location.coordinate.longitude
         )
         let accuracy = location.horizontalAccuracy // ok
         let altitude = location.altitude // ok
         let altitudeAccuracy = location.verticalAccuracy // ok
         let course = location.course
         let courseAccuracy = location.courseAccuracy
         let speed = location.speed // ok
         let speedAccuracy = location.speedAccuracy // ok
         */
        
        let coordinates = Coordinates(
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude
        )
        return LocationData(
            accuracy: location.horizontalAccuracy,
            altitude: location.altitude,
            altitudeAccuracy: location.verticalAccuracy,
            heading: heading.trueHeading,
            speed: location.speed,
            coordinates: coordinates
        )
    }
}
