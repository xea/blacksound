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
        devices: [],
        user: {
            hasSession: false,
            name: "Some random dude",
            logoutUri: "/api/logout",
            streamingEnabled: undefined
        },
        playlist: [
            //{ title: "Metallica / One", uri: "spotify:track:asdfasdf" }
        ],
        stream: {
            currentTrackLength: 0,
            playbackPosition: 0
        },
        playbackPosition: 0,
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
                        vm.stream.currentTrackLength = response.currentTrackLength;
                        vm.stream.playbackPosition = response.playbackPosition;
                        vm.devices = response.devices;
                        vm.playlist = response.playlist;

                        if (vm.playlist.length > 0) {
                            setTimeout(() => {
                                vm.playlist.shift();
                            }, (vm.stream.currentTrackLength - vm.stream.playbackPosition) * 1000);
                        }
                    } else {
                        vm.spotify.redirectUri = response.redirectUri;
                        vm.user.streamingEnabled = false;
                    }

                    vm.debugMessage = "Ready";
                });
        },
        loadPlaylist: function() {
            vm.debugMessage = "Loading playlist";

            fetch("/api/playlist")
                .then(response => response.json())
                .then(response => {
                    vm.debugMessage = "Playlist loaded";
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
        resumeStreaming: function() {
            vm.debugMessage = "Resuming streaming";

            fetch("/api/resume")
                .then(response => response.json())
                .then(function (response) {
                    vm.user.streamingEnabled = response.streamingEnabled;
                    vm.debugMessage = "Streaming started";
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
        },
        setActiveDevice: function(deviceId) {
            vm.debugMessage = "Setting active device";

            let request = { deviceId: deviceId };

            fetch("/api/set-device", { method: "POST", body: JSON.stringify(request) })
                .then(response => response.json())
                .then(function (response) {
                    vm.debugMessage = "Active device set";
                });
        },
        updatePlaybackPosition: function() {
            if (this.stream.currentTrackLength > 0) {
                this.stream.playbackPosition += 1;
            }

            if (this.stream.playbackPosition >= this.stream.currentTrackLength) {
                this.stream.playbackPosition = 0;
            }
        }
    },
    computed: {
        formattedPlaybackPosition: function() {
            let minutes = "" + Math.trunc(this.stream.playbackPosition / 60);
            let seconds = ("" + this.stream.playbackPosition % 60).padStart(2, '0');

            return minutes + ":" + seconds;
        }
    },
    mounted: function() {
        setInterval(() => {
            this.updatePlaybackPosition();
        }, 1000);
        this.$nextTick(function() {
            this.init();
        })
    }
});
