function initMap() {
	var farm = {lat: 51.308423, lng: 1.102136};

	var map = new google.maps.Map(document.getElementById('map'), {
		zoom: 17,
		center: farm,
		mapTypeId: 'satellite'
	});

	$.getJSON("http://localhost:8080/json/markers", function(result) {
  		$.each(result, function(i, next){
			var marker = new google.maps.Marker({
				map: map,
				position: {lat: next.latitude, lng: next.longitude},
				icon: {url: next.icon, scaledSize: new google.maps.Size(28, 28)},
				animation: google.maps.Animation.DROP,
				title: next.title,
				url: next.url
		  	});

			google.maps.event.addListener(marker, 'click', function() {
				window.location.href = this.url;
			});
		});
	});
}