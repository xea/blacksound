let _ = new Vue({
    el: '#app',
    data: {
        statusMessage: "Uninitialized",
        spotify: {
            authorizationCode: undefined,
            currentTrack: undefined,
            redirectUri: undefined,
            trackUri: undefined
        },
        user: {
            hasSession: false,
            name: "<<Anonymous>>"
        }
    },
    methods: {
        init: function() {
            let vm = this;
            vm.statusMessage = "Initializing";

            fetch("/api/status")
                .then(response => response.json())
                .then(function (response) {
                    vm.user.hasSession = response.hasSession;
                    vm.statusMessage = response.status;

                    if (response.hasSession) {
                        vm.user.name = response.name;
                        vm.spotify.currentTrack = response.currentTrack;
                    } else {
                        vm.spotify.redirectUri = response.redirectUri;
                    }
                });
        },
        playTrack: function() {
            let vm = this;
            vm.statusMessage = "Playing track";

            let request = { trackUri: vm.spotify.trackId };

            fetch("/api/play", { method: "POST", body: JSON.stringify(request) })
                .then(response => response.json())
                .then(response => {
                    vm.statusMessage = "Playing your stupid track";
                })
        },
        pause: function() {
            let vm = this;
            vm.statusMessage = "Pausing track";

            fetch("/api/pause")
                .then(response => response.json())
                .then(response => {
                    vm.statusMessage = "Playback paused";
                })
        },
        sendAuthorizationCode: function() {
            let vm = this;
            vm.statusMessage = "Sending authorization code";

            fetch("/spotify-redirect?code=" + vm.spotify.authorizationCode)
                .then(response => response.json())
                .then(response => {
                    vm.statusMessage = "Authorization code processed";
                })
        }
    },
    mounted: function() {
        this.$nextTick(function() {
            this.init();
        })
    }
    /*
    methods: {
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
     */
});