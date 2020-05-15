let _ = new Vue({
    el: '#app',
    data: {
        message: "Vue is working",
        redirectUri: undefined
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
            fetch("/api/play", { method: "POST" })
                .then(function(response) {
                    console.log(response);
                })
        },
        status: function() {
            fetch("/api/status")
                .then(function(response) {
                    console.log(response);
                })
        }
    }

});