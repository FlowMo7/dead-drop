# Dead Drop Service

**Send One-Time Secrets in a secure way**

A Dead Drop service written in Kotlin / KTor.

This service uses [sjcl](https://github.com/bitwiseshiftleft/sjcl) on client-side to encrypt the data, stores the
encrypted data on the server (while never sharing the password with the server), and generates a link to get the data
(once) again.

This is the source-code of [drop.moetz.dev](https://drop.moetz.dev).

## Setup

The docker image can be found here: [hub.docker.com/r/flowmo7/dead-drop](https://hub.docker.com/r/flowmo7/dead-drop).

Possible environment variables:

* `DOMAIN`: the domain this application is available at, e.g. `drop.example.org`
* `IS_HTTPS`: Whether this application is available as HTTPS / behind an HTTPS reverse proxy (which it should be). Default to `true`.
* `PATH_PREFIX`: When the application is available on a sub-path of the given domain (routed by a reverse proxy, e.g.), the path needs to be set here. Defaults to no path.
* `FILE_KEEP_TIME_IN_HOURS`: The number of hours to keep a drop-record. Defaults to `24`.
* `SHOW_GITHUB_LINK_IN_FOOTER`: Whether the GitHub link should be visible in the footer. Defaults to `true`.

### Data persistence

If you want to persist the encrypted storage, and map it out of the docker container, thw following mounting points are available:

* `/var/dead-drop/data`: Is the directory that contains the encrypted data (for at most 24 hours)
* `/var/dead-drop/key/key.secret`: Is the file that contains the key for the server-side encryption (the data stored in 
the data directory is encrypted another time before persisted in the given path).

### Example docker-compose.yml

```yaml
services:
  dead-drop:
    image: "flowmo7/dead-drop:master"
    restart: unless-stopped
    ports:
      - 8080:8080 #Should be behind an SSL reverse proxy
    environment:
      - DOMAIN=drop.example.org
    volumes:
      - /srv/docker/dead-drop/data:/var/dead-drop/data:rw
      - /srv/docker/dead-drop/key:/var/dead-drop/key:rw
```

## Acknowledgments

This project is heavily inspired by [BillKeenan/dead-drop-python](https://github.com/BillKeenan/dead-drop-python), which
I heavily used (self-hosted) before creating this service.

# LICENSE

```
Copyright 2021 Florian Mötz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
