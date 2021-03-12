/*
This code centers the map on the closest bus and then for each bus, drops a
pin at the bus' location.
*/

let map;
function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        center: { lat: parseFloat(busLocations[0].LATITUDE), lng: parseFloat(busLocations[0].LONGITUDE) },
        zoom: 15,
        scrollwheel: false
    });
    let bus = "/bus.png";

    for (i=0; i<busLocations.length; i++){
        var marker = new google.maps.Marker({
            position: { lat: parseFloat(busLocations[i].LATITUDE), lng: parseFloat(busLocations[i].LONGITUDE) },
            map: map,
            icon: {
                    url: bus,
                    scaledSize: new google.maps.Size(20, 20)
            }
        });
    }
}