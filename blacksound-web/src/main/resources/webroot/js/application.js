let _ = new Vue({
    el: '#app',
    data: {
        statusMessage: "",
        redirectUri: undefined,
        trackUri: undefined,
    },
    mounted: function() {
        fetch("/api/status")
            .then(function (response) {
                this.statusMessage = "Status OK";
            });
    },
    methods: {
        loadSettings: function() {
            fetch("/api/redirect-uri")
                .then(response => response.json())
                .then(response => this.redirectUri = response.redirectUri)
        },
        nextSong: function() {
            fetch("/api/next-song", { method: "POST" })
                .then(function(response) {
                    console.log(response);
                })
        },
        playStream: function() {
            let request = {
                trackUri: this.trackUri
            };

            fetch("/api/play", {
                method: "POST",
                body: JSON.stringify(request)
            }).then(function(response) {
                console.log(response);
            })
        },
    }

});