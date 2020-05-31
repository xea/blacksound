"use strict";

let vm = new Vue({
    el: '#app',
    data: {
        debugMessage: "Uninitialized",
        spotify: {
            currentTrackTitle: undefined,
            redirectUri: undefined,
            trackUri: undefined
        },
        user: {
            hasSession: false,
            name: "Some random dude",
            logoutUri: "/api/logout",
            streamingEnabled: undefined
        },
        playlist: [
            { title: "Metallica / One", uri: "spotify:track:asdfasdf" }
        ],
        searchExpression: undefined,
        searchResult: undefined
    },
    methods: {
        init: function() {
            vm.debugMessage = "Initializing";

            fetch("/api/status")
                .then(response => response.json())
                .then(function (response) {
                    vm.user.hasSession = response.hasSession;
                    vm.debugMessage = response.status;

                    if (response.hasSession) {
                        vm.user.name = response.name;
                        vm.user.streamingEnabled = response.streamingEnabled;
                        vm.spotify.currentTrack = response.currentTrack;
                        vm.playlist = response.playlist;
                    } else {
                        vm.spotify.redirectUri = response.redirectUri;
                        vm.user.streamingEnabled = false;
                    }

                    vm.debugMessage = "Ready";
                });
        },
        startStreaming: function() {
            vm.debugMessage = "Starting streaming";

            fetch("/api/resume")
                .then(response => response.json())
                .then(function (response) {
                    vm.user.streamingEnabled = response.streamingEnabled;
                    vm.debugMessage = "Streaming started";
                });
        },
        pauseStreaming: function() {
            vm.debugMessage = "Pausing streaming";

            fetch("/api/pause")
                .then(response => response.json())
                .then(function (response) {
                    vm.user.streamingEnabled = response.streamingEnabled;
                    vm.debugMessage = "Streaming paused";
                });
        },
        queueTrack: function() {
            vm.debugMessage = "Queueing track";

            let request = { trackUri: vm.spotify.trackUri };

            fetch("/api/queue", { method: "POST", body: JSON.stringify(request) })
                .then(response => response.json())
                .then(function (response) {
                    vm.playlist = response.playlist;
                    vm.spotify.trackUri = undefined;
                    vm.debugMessage = "Track queued";
                });
        },
        searchSong: function() {
            vm.debugMessage = "Searching for song";

            let request = { searchExpression: vm.searchExpression };

            fetch("/api/search", { method: "POST", body: JSON.stringify(request) })
                .then(response => response.json())
                .then(function (response) {

                    vm.searchResult = response.result;

                    vm.debugMessage = "Search completed";
                });
        }
    },
    mounted: function() {
        this.$nextTick(function() {
            this.init();
        })
    }
});
