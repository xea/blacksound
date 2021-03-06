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
            streamingEnabled: false
        },
        playlist: [
            //{ title: "Metallica / One", uri: "spotify:track:asdfasdf" }
        ],
        stream: {
            currentTrackLength: 0,
            playbackPosition: 0
        },
        searchExpression: undefined,
        searchResult: undefined,
        activeUsers: []
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
                        vm.activeUsers = response.activeUsers;
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

                    vm.init();
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
        setActiveDevice: function(event) {
            vm.debugMessage = "Setting active device";

            let request = { deviceId: event.target.value };

            fetch("/api/set-device", { method: "POST", body: JSON.stringify(request) })
                .then(response => response.json())
                .then(function (response) {
                    vm.devices = response.devices;
                    vm.debugMessage = "Active device set";
                });
        },
        updatePlaybackPosition: function() {
            if (this.stream.currentTrackLength > 0) {
                this.stream.playbackPosition += 1;
            }

            if (this.stream.playbackPosition >= this.stream.currentTrackLength) {
                this.stream.playbackPosition = 0;

                if (this.playlist.length > 0) {
                    this.playlist.shift();
                }
            }
        }
    },
    computed: {
        formattedPlaybackPosition: function() {
            let minutes = "" + Math.trunc(this.stream.playbackPosition / 60);
            let seconds = ("" + this.stream.playbackPosition % 60).padStart(2, '0');

            return minutes + ":" + seconds;
        },
        isStreaming: function () {
            return this.playlist.length > 0;
        }
    },
    mounted: function() {
        setInterval(() => this.updatePlaybackPosition(), 1000);
        setInterval(() => this.init(), 5000);
        this.$nextTick(function() {
            this.init();
        })
    }
});
