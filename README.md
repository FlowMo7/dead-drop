# Dead Drop Service

**Send One-Time Secrets in a secure way**

A Dead Drop service written in Kotlin / KTor.

This service uses [sjcl](https://github.com/bitwiseshiftleft/sjcl) on client-side to encrypt the data, stores the
encrypted data on the server (while never sharing the password with the server), and generates a link to get the data
(once) again.

This is the source-code of [drop.moetz.dev](https://drop.moetz.dev).

## Setup using Docker

The docker image can be found here: [hub.docker.com/r/flowmo7/dead-drop](https://hub.docker.com/r/flowmo7/dead-drop).

Possible environment variables:

* `DOMAIN`: the domain this application is available at, e.g. `drop.example.org`
* `IS_HTTPS`: Whether this application is available as HTTPS / behind an HTTPS reverse proxy (which it should be),
  e.g. `true`
* `DATA_DIRECTORY`: The directory to store the data in (within the docker image), e.g. `/var/dead-drop/data`
* `ENCRYPTION_KEY_PATH`: The file-path to store the _server-side_ encryption key at (within the docker image),
  e.g. `/var/dead-drop/key/key.secret`
* `FILE_KEEP_TIME_IN_HOURS`: The number of hours to keep a drop-record. Defaults to 24.

## Example docker-compose.yml

```yaml
services:
  dead-drop:
    image: "flowmo7/dead-drop:master"
    restart: unless-stopped
    ports:
      - 8080:8080 #Should be behind an SSL reverse proxy
    environment:
      - DOMAIN=drop.example.org
      - IS_HTTPS=true
      - DATA_DIRECTORY=/var/dead-drop/data
      - ENCRYPTION_KEY_PATH=/var/dead-drop/key/key.secret
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
