//
// EventMap.swift
// iosApp
//
// Created by Elesey Razumovskiy on 5/21/25.
// Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp
import GoogleMaps

struct EventMap: View {
    var component: MapComponent
    var onEventSelected: (EventAbs) -> Void
    var onPlaceSelected: (MVPPlace) -> Void
    var canClickPOI: Bool
    var focusedEvent: EventAbs?
    var focusedLocation: LatLng?
    var revealCenter: CGPoint
    
    @State private var currentLocation: LatLng? = nil
    @State private var events: [EventAbs] = []
    @State private var suggestions: [MVPPlace] = []
    @State private var searchText: String = ""
    @State private var places: [MVPPlace] = []
    @State private var reveal: Bool = false
    @State private var searchTask: Task<Void, Never>? = nil
    
    init(
        component: MapComponent,
        onEventSelected: @escaping (EventAbs) -> Void,
        onPlaceSelected: @escaping (MVPPlace) -> Void,
        canClickPOI: Bool,
        focusedLocation: LatLng?,
        focusedEvent: EventAbs?,
        revealCenter: CGPoint
    ) {
        self.component = component
        self.onEventSelected = onEventSelected
        self.onPlaceSelected = onPlaceSelected
        self.canClickPOI = canClickPOI
        self.focusedLocation = focusedLocation
        self.focusedEvent = focusedEvent
        self.revealCenter = revealCenter
    }
    
    var body: some View {
        Observing(
            component.currentLocation,
            component.events,
            component.isMapVisible
        ) { (loc: LatLng?, ev: [EventAbs], reveal: KotlinBoolean) in
            ZStack(alignment: .top) {
                GoogleMapView(
                    component: component,
                    events: ev,
                    canClickPOI: canClickPOI,
                    focusedLocation: focusedLocation,
                    focusedEvent: focusedEvent,
                    onEventSelected: onEventSelected,
                    onPlaceSelected: onPlaceSelected,
                    places: places,
                    revealCenter: revealCenter
                )
                .edgesIgnoringSafeArea(.all)
                .opacity(reveal.boolValue ? 1.0 : 0.0)
                .allowsHitTesting(reveal.boolValue)
                .animation(.easeInOut(duration: 0.5), value: reveal.boolValue)
                
                if canClickPOI && reveal.boolValue {
                    MapSearchBar(
                        text: $searchText,
                        suggestions: suggestions,
                        onSearch: { query in
                            Task {
                                if let currentLoc = loc {
                                    // Clear previous search results
                                    places = []
                                    // Add new search results
                                    places = try await component.searchPlaces(
                                        query: query,
                                        latLng: LatLng(latitude: currentLoc.latitude, longitude: currentLoc.longitude)
                                    )
                                }
                                searchText = ""
                            }
                        },
                        onSuggestionTap: { place in
                            Task {
                                if place.id == "Query" {
                                    if let currentLoc = loc {
                                        // Clear previous results before new search
                                        places = []
                                        places = try await component.searchPlaces(
                                            query: place.name,
                                            latLng: LatLng(latitude: currentLoc.latitude, longitude: currentLoc.longitude)
                                        )
                                    } else {
                                        places = []
                                    }
                                } else {
                                    let placeDetails = try await component.getPlace(placeId: place.id)
                                    if let placeDetails = placeDetails {
                                        // Clear previous results and set new single place
                                        places = [placeDetails]
                                    }
                                }
                            }
                        }
,
                        onTextChanged: { newValue in
                            searchTask?.cancel()
                            
                            guard !newValue.isEmpty else {
                                suggestions = []
                                return
                            }
                            
                            // Create new debounced search task
                            searchTask = Task {
                                do {
                                    // Wait for 300ms (debounce delay)
                                    try await Task.sleep(nanoseconds: 300_000_000)
                                    
                                    // Check if task was cancelled during the delay
                                    try Task.checkCancellation()
                                    
                                    if let currentLoc = loc {
                                        suggestions = try await component.suggestPlaces(
                                            query: newValue,
                                            latLng: LatLng(latitude: currentLoc.latitude, longitude: currentLoc.longitude)
                                        )
                                    }
                                } catch is CancellationError {
                                    // Task was cancelled, do nothing
                                    print("Search task cancelled for: \(newValue)")
                                } catch {
                                    print("Search error: \(error)")
                                }
                            }
                        }
                    )
                    .zIndex(1)
                }
            }
            .background(Color.clear)
        }
    }
}

struct GoogleMapView: UIViewRepresentable {
    let component: MapComponent
    let currentLocation: LatLng?
    let events: [EventAbs]
    let canClickPOI: Bool
    let focusedLocation: LatLng?
    let focusedEvent: EventAbs?
    let onEventSelected: (EventAbs) -> Void
    let onPlaceSelected: (MVPPlace) -> Void
    let places: [MVPPlace]
    let revealCenter: CGPoint
    
    init(
        component: MapComponent,
        currentLocation: LatLng? = nil,
        events: [EventAbs],
        canClickPOI: Bool = false,
        focusedLocation: LatLng? = nil,
        focusedEvent: EventAbs? = nil,
        onEventSelected: @escaping (EventAbs) -> Void,
        onPlaceSelected: @escaping (MVPPlace) -> Void,
        places: [MVPPlace],
        revealCenter: CGPoint
    ){
        self.component = component
        self.currentLocation = currentLocation
        self.events = events
        self.canClickPOI = canClickPOI
        self.focusedLocation = focusedLocation
        self.focusedEvent = focusedEvent
        self.onEventSelected = onEventSelected
        self.onPlaceSelected = onPlaceSelected
        self.places = places
        self.revealCenter = revealCenter
    }
    
    func makeUIView(context: Context) -> GMSMapView {
        let camera: GMSCameraPosition
        if let f = focusedLocation {
            camera = .camera(withLatitude: f.latitude, longitude: f.longitude, zoom: 12)
        } else if let loc = currentLocation {
            camera = .camera(withLatitude: loc.latitude, longitude: loc.longitude, zoom: 12)
        } else {
            camera = .camera(withLatitude: 0.0, longitude: 0.0, zoom: 2)
        }
        
        let options = GMSMapViewOptions()
        options.camera = camera
        options.frame = .zero
        let mapView = GMSMapView.init(options: options)
        
        // Configure map for maximum POI visibility
        mapView.isMyLocationEnabled = true
        mapView.delegate = context.coordinator
        mapView.mapType = .normal
        
        // Enable all POI types
        mapView.settings.consumesGesturesInView = false
        mapView.settings.scrollGestures = true
        mapView.settings.zoomGestures = true
        
        // Debug: Check if POI clicks are working
        print("Map created with canClickPOI: \(canClickPOI)")
        
        return mapView
    }


    
    func updateUIView(_ mapView: GMSMapView, context: Context) {
        // Clear place markers when new places array comes in
        if !places.isEmpty {
            context.coordinator.clearPlaceMarkers()
            context.coordinator.currentPOIMarker?.map = nil
            context.coordinator.currentPOIMarker = nil
        }
        
        if let fe = focusedEvent, context.coordinator.lastEventId != fe.id {
            mapView.clear()
            context.coordinator.currentPOIMarker = nil
            context.coordinator.placeMarkers.removeAll()
            
            let pos = CLLocationCoordinate2D(latitude: fe.lat, longitude: fe.long)
            let camera = GMSCameraPosition.camera(withTarget: pos, zoom: 12)
            mapView.animate(to: camera)
            context.coordinator.lastEventId = fe.id
            
            let coord = CLLocationCoordinate2D(latitude: fe.lat, longitude: fe.long)
            let m = GMSMarker(position: coord)
            m.title = fe.name
            m.snippet = "\(fe.fieldType) – $\(fe.price)"
            m.userData = fe
            m.map = mapView
            
        } else if let fc = focusedLocation,
                  context.coordinator.lastFocus ?? (0.0, 0.0) != (fc.latitude, fc.longitude) {
            mapView.clear()
            context.coordinator.currentPOIMarker = nil
            context.coordinator.placeMarkers.removeAll()
            
            let pos = CLLocationCoordinate2D(latitude: fc.latitude, longitude: fc.longitude)
            let camera = GMSCameraPosition.camera(withTarget: pos, zoom: 12)
            mapView.animate(to: camera)
            context.coordinator.lastFocus = (fc.latitude, fc.longitude)
        }
        
        // Add event markers (only when not in POI selection mode)
        if !canClickPOI {
            for event in events {
                let coord = CLLocationCoordinate2D(latitude: event.lat, longitude: event.long)
                let m = GMSMarker(position: coord)
                m.title = event.name
                m.snippet = "\(event.fieldType) – $\(event.price)"
                m.userData = event
                m.map = mapView
            }
        }
        
        // Add suggestion place markers and track them
        for place in places {
            let coord = CLLocationCoordinate2D(latitude: place.lat, longitude: place.long)
            let marker = GMSMarker(position: coord)
            marker.title = place.name
            marker.userData = place
            marker.map = mapView
            
            // Track this marker
            context.coordinator.placeMarkers.append(marker)
            
            // Animate to the suggestion place if it's the only one
            if places.count == 1 {
                let camera = GMSCameraPosition.camera(withTarget: coord, zoom: 15)
                mapView.animate(to: camera)
                // Show the info window automatically for suggestion places
                mapView.selectedMarker = marker
            }
        }
    }



    
    func makeCoordinator() -> Coordinator {
        Coordinator(
            parent: self,
            onEventSelected: onEventSelected,
            onPlaceSelected: onPlaceSelected
        )
    }
    
    private func cameraPosition(for coord: CLLocationCoordinate2D, zoom: Float = 12) -> GMSCameraPosition {
        GMSCameraPosition.camera(withTarget: coord, zoom: zoom)
    }
}

class Coordinator: NSObject, GMSMapViewDelegate {
    let parent: GoogleMapView
    var lastFocus: (Double, Double)?
    var lastEventId: String?
    var selectedMarker: GMSMarker?
    var currentPOIMarker: GMSMarker? // Track the current POI marker
    var placeMarkers: [GMSMarker] = [] // Track place markers from searches/suggestions
    
    init(
        parent: GoogleMapView,
        onEventSelected: @escaping (EventAbs) -> Void,
        onPlaceSelected: @escaping (MVPPlace) -> Void
    ) {
        self.parent = parent
    }
    
    // Helper method to clear place markers
    func clearPlaceMarkers() {
        placeMarkers.forEach { $0.map = nil }
        placeMarkers.removeAll()
    }
    
    // Handle POI clicks
    func mapView(
        _ mapView: GMSMapView,
        didTapPOIWithPlaceID placeID: String,
        name: String,
        location: CLLocationCoordinate2D
    ) {
        print("POI tapped: \(name) with ID: \(placeID)")
        guard parent.canClickPOI else {
            print("POI clicks disabled")
            return
        }
        
        print("Processing POI click for: \(name)")
        
        // Clear the previous POI marker if it exists
        currentPOIMarker?.map = nil
        currentPOIMarker = nil
        
        mapView.animate(to: GMSCameraPosition.camera(withTarget: location, zoom: 12))
        
        // Create a new marker for the POI
        let marker = GMSMarker(position: location)
        marker.title = name
        marker.userData = placeID
        marker.map = mapView
        
        // Track this as the current POI marker
        currentPOIMarker = marker
        
        // Show the info window
        mapView.selectedMarker = marker
        selectedMarker = marker
    }
    
    // Handle marker tap (show info window)
    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        mapView.selectedMarker = marker
        selectedMarker = marker
        return true
    }
    
    // Handle info window tap (trigger the action)
    func mapView(_ mapView: GMSMapView, didTapInfoWindowOf marker: GMSMarker) {
        if let event = marker.userData as? EventAbs {
            parent.onEventSelected(event)
        } else if let placeID = marker.userData as? String {
            Task {
                do {
                    let place = try await self.parent.component.getPlace(placeId: placeID)
                    if let place = place {
                        await MainActor.run {
                            self.parent.onPlaceSelected(place)
                        }
                    }
                } catch {
                    print("Error getting place details: \(error)")
                }
            }
        } else if let place = marker.userData as? MVPPlace {
            parent.onPlaceSelected(place)
        }
    }
    
    // Clear selected markers when tapping elsewhere
    func mapView(_ mapView: GMSMapView, didTapAt coordinate: CLLocationCoordinate2D) {
        selectedMarker = nil
        // Optionally clear POI marker when tapping empty space
        // currentPOIMarker?.map = nil
        // currentPOIMarker = nil
    }
}


struct MapSearchBar: View {
    @Binding var text: String
    let suggestions: [MVPPlace]
    let onSearch: (String) -> Void
    let onSuggestionTap: (MVPPlace) -> Void
    let onTextChanged: (String) -> Void
    
    @FocusState private var isTextFieldFocused: Bool
    
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                TextField("Search places", text: $text)
                    .focused($isTextFieldFocused)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .onChange(of: text) { newValue in
                        onTextChanged(newValue)
                    }
                    .onSubmit {
                        onSearch(text)
                        text = ""
                        isTextFieldFocused = false
                    }
                    .padding(8)
                
                Button("Search") {
                    onSearch(text)
                    text = ""
                    isTextFieldFocused = false
                }
                .padding(.trailing, 8)
            }
            .background(Color(.systemBackground))
            .cornerRadius(8)
            .shadow(radius: 2)
            
            if !suggestions.isEmpty {
                ScrollView {
                    VStack(spacing: 0) {
                        ForEach(suggestions, id: \.id) { place in
                            Button(action: {
                                onSuggestionTap(place)
                                isTextFieldFocused = false
                            }) {
                                HStack {
                                    Text(place.name)
                                        .foregroundColor(.primary)
                                    Spacer()
                                }
                                .padding(.vertical, 8)
                                .padding(.horizontal)
                            }
                            Divider()
                        }
                    }
                }
                .frame(maxHeight: 200)
                .background(Color(.systemBackground))
                .cornerRadius(8)
                .shadow(radius: 2)
            }
        }
        .padding(.horizontal)
    }
}



// Custom circular reveal transition
extension AnyTransition {
    static func circularReveal(center: CGPoint) -> AnyTransition {
        .modifier(
            active: CircularRevealModifier(center: center, scale: 0),
            identity: CircularRevealModifier(center: center, scale: 1)
        )
    }
}

struct CircularRevealModifier: ViewModifier {
    let center: CGPoint
    let scale: CGFloat
    
    func body(content: Content) -> some View {
        content
            .clipShape(Circle()
                .scale(scale)
                .offset(x: center.x - UIScreen.main.bounds.width/2,
                       y: center.y - UIScreen.main.bounds.height/2))
    }
}
