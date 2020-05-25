let _ = new Vue({
    el: '#app',
    data: {
        statusMessage: "Uninitialized",
        spotify: {
            authorizationCode: undefined,
            redirectUri: undefined
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
                    } else {
                        vm.spotify.redirectUri = response.redirectUri;
                    }
                });
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
    data: {
        statusMessage: "Init",
        redirectUri: undefined,
        trackUri: undefined,
    },
    mounted: function() {
        this.$nextTick(function() {
            fetch("/api/status")
                .then(function (response) {
                    this.statusMessage = "Status OK";
                });
        })
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
     */
});