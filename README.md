# koe

Tiny, minimal dependency and embeddable library implementing Discord voice protocol built on [Netty](https://netty.io), aiming for high performance and reduced GC usage.

[Get it on JitPack](https://jitpack.io/#moe.kyokobot/koe)

#### Dependencies
- Netty
- slf4j
- Java 11+ (could be backported to Java 8 with minor code changes)

#### Features

- Support for voice gateway ~~v3 and~~ v4.
- Easily extendable for stuff such as support for codecs other than Opus or video sending, if Discord ever decides to support it on bots.
- Basic RTCP support for measuring packet loss and other stuff.

#### Non-goals / won't do

- Encoding - Koe only implements voice server communication, not voice handling itself, so it only accepts Opus frames, you have set up an encoder yourself, use [lavaplayer](https://github.com/sedmelluq/lavaplayer), libav/ffmpeg or anything else.
- Voice receiving support - [it's not supported by Discord anyway](https://github.com/discordapp/discord-api-docs/issues/808#issuecomment-458863743), although someone could implement it by registering hooks.

#### Extensions

- [UDP-Queue](https://github.com/KyokoBot/koe/tree/master/ext-udpqueue)

#### Credits

[@TheAkio](https://github.com/TheAkio) for name idea.

Koe includes modified/stripped-down parts based on following open-source projects:

- [tweetnacl-java](https://github.com/InstantWebP2P/tweetnacl-java) (Poly1305, SecretBox)
- [nanojson](https://github.com/mmastrac/nanojson) (modified for bytebuf support, changed the API a bit and etc.)
