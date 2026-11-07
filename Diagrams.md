# Diagrams

A dead-drop is a service which stores encrypted messages, which can be retrieved exactly once.
If the message has been retrieved with a wrong password (and/or wrong person), the data is deleted (and cannot be
retrieved by the correct person / correct password anymore).

Therefore:

* when retrieving the data successfully, you know, only you got it.
* when the data could not be retrieved, you can expect someone else tried it already.

## Successful retrieval

This diagram shows the sequence of a successful exchange from Alice to Bob:

```mermaid
sequenceDiagram
    box Alice' PC
        actor Alice
        participant AliceBrowser as Alice' Browser
    end
    participant Server as DeadDrop Server
    box Bob's PC
        participant BobBrowser as Bob's Browser
        actor Bob
    end

    Alice->>AliceBrowser: Enters a message<br/>as a dead drop
    activate AliceBrowser
    AliceBrowser->>AliceBrowser: Generated a password
    AliceBrowser->>AliceBrowser: Encrypts the message<br/>with the password
    AliceBrowser->>Server:Sends encypted data
    activate Server
    Server->>AliceBrowser: Returns ID for pickup
    AliceBrowser->>Alice: Shows password and<br/>link for pickup
    deactivate AliceBrowser

    Alice-->>Bob: Sends link and password for pickup

    Bob->>BobBrowser: Opens the link
    activate BobBrowser
    Bob->>BobBrowser: Enters password
    BobBrowser->>Server: Loads payload for ID<br/>without sending the password
    Server->>Server: Deletes the payload
    Server->>BobBrowser: Responds encrypted payload
    deactivate Server
    BobBrowser->>BobBrowser: Decrypts the payload using the password Bob entered
    BobBrowser->>Bob: Displays Bob the decrypted message
    deactivate BobBrowser
```

## Message (link and password) is intercepted

In this diagram, a malicious actor (`Eve`) got a hand of the message that Alice sent to bob, which contained both the
link and the password:

```mermaid
sequenceDiagram
    box Alice' PC
        actor Alice
        participant AliceBrowser as Alice' Browser
    end
    participant Server as DeadDrop Server

    box Malicious Actor (Eve)
        participant EveBrowser as Eve's Browser
        actor Eve
    end

    box Bob's PC
        participant BobBrowser as Bob's Browser
        actor Bob
    end

    Alice->>AliceBrowser: Enters a message<br/>as a dead drop
    activate AliceBrowser
    AliceBrowser->>AliceBrowser: Generated a password
    AliceBrowser->>AliceBrowser: Encrypts the message<br/>with the password
    AliceBrowser->>Server:Sends encypted data
    activate Server
    Server->>AliceBrowser: Returns ID for pickup
    AliceBrowser->>Alice: Shows password and<br/>link for pickup
    deactivate AliceBrowser

    Alice-->>Bob: Sends link and password for pickup
    

    Note over Alice, Bob: Eve intercepts the message
    Eve->>EveBrowser: Opens the link

    activate EveBrowser
    Eve->>EveBrowser: Enters password
    EveBrowser->>Server: Loads payload for ID<br/>without sending the password
    Server->>Server: Deletes the payload
    Server->>EveBrowser: Responds encrypted payload
    deactivate Server
    EveBrowser->>EveBrowser: Decrypts the payload using the password Eve entered
    EveBrowser->>Eve: Displays Eve the decrypted message



    Bob->>BobBrowser: Opens the link
    activate BobBrowser
    Bob->>BobBrowser: Enters password
    BobBrowser->>Server: Loads payload for ID<br/>without sending the password
    Server->>BobBrowser: Responds no payload
    
    BobBrowser->>Bob: Displays Bob an error 
    deactivate BobBrowser
    Note over Bob,BobBrowser: Bob must expect that the<br/>message is now in evil hands
```

## Message (only link) is intercepted

In this example, the malicious actor (`Eve`) got a hold of one of the two messages which Alice sent to Bob, and will
with her attempt to guess the password destroy the drop. Bob can no longer retrieve the message (even though only he has
the correct password), and must expect that the message is lost (and possibly decrypted).

```mermaid
sequenceDiagram
    box Alice' PC
        actor Alice
        participant AliceBrowser as Alice' Browser
    end
    participant Server as DeadDrop Server

    box Malicious Actor (Eve)
        participant EveBrowser as Eve's Browser
        actor Eve
    end

    box Bob's PC
        participant BobBrowser as Bob's Browser
        actor Bob
    end

    Alice->>AliceBrowser: Enters a message<br/>as a dead drop
    activate AliceBrowser
    AliceBrowser->>AliceBrowser: Generated a password
    AliceBrowser->>AliceBrowser: Encrypts the message<br/>with the password
    AliceBrowser->>Server:Sends encypted data
    activate Server
    Server->>AliceBrowser: Returns ID for pickup
    AliceBrowser->>Alice: Shows password and<br/>link for pickup
    deactivate AliceBrowser

    Alice-->>Bob: Sends link via E-Mail
    Alice-->>Bob: Sends password via SMS
    

    Note over Alice, Bob: Eve intercepts the E-Mail
    Eve->>EveBrowser: Opens the link

    activate EveBrowser
    Eve->>EveBrowser: Tries out a random password
    EveBrowser->>Server: Loads payload for ID<br/>without sending the password
    Server->>Server: Deletes the payload
    Server->>EveBrowser: Responds encrypted payload
    deactivate Server
    EveBrowser->>EveBrowser: Tries to decrypt the payload, but fails<br/>(due to a wrong password)
    EveBrowser->>Eve: Displays Eve an error



    Bob->>BobBrowser: Opens the link
    activate BobBrowser
    Bob->>BobBrowser: Enters password
    BobBrowser->>Server: Loads payload for ID<br/>without sending the password
    Server->>BobBrowser: Responds no payload
    
    BobBrowser->>Bob: Displays Bob an error
    deactivate BobBrowser
    Note over Bob,BobBrowser: Bob must expect that the<br/>message is now in evil hands,<br/>even though the message was<br/>never successfully decrypted
```

## Message is intercepted at a later point

This diagram shows what happens, if at a later point a malicious actor (`Eve`) gets a hold on the link and password
Alice sent to Bob (e.g. E-Mail account or messaging account got hacked at a later point).

While the message (and therefore link and password) are available to Eve, she can not do anything with this data (in
comparison to what would happen if Alice would have sent Bob the secret message directly).

```mermaid
sequenceDiagram
    box Alice' PC
        actor Alice
        participant AliceBrowser as Alice' Browser
    end
    participant Server as DeadDrop Server

    box Malicious Actor (Eve)
        participant EveBrowser as Eve's Browser
        actor Eve
    end

    box Bob's PC
        participant BobBrowser as Bob's Browser
        actor Bob
    end

    Alice->>AliceBrowser: Enters a message<br/>as a dead drop
    activate AliceBrowser
    AliceBrowser->>AliceBrowser: Generated a password
    AliceBrowser->>AliceBrowser: Encrypts the message<br/>with the password
    AliceBrowser->>Server:Sends encypted data
    activate Server
    Server->>AliceBrowser: Returns ID for pickup
    AliceBrowser->>Alice: Shows password and<br/>link for pickup
    deactivate AliceBrowser

    Alice-->>Bob: Sends link and password for pickup via E-Mail


    Bob->>BobBrowser: Opens the link
    activate BobBrowser
    Bob->>BobBrowser: Enters password
    BobBrowser->>Server: Loads payload for ID<br/>without sending the password
    Server->>Server: Deletes the payload
    Server->>BobBrowser: Responds encrypted payload
    deactivate Server
    BobBrowser->>BobBrowser: Decrypts the payload using the password Bob entered
    BobBrowser->>Bob: Displays Bob the decrypted message
    deactivate BobBrowser

    Note over Alice, Bob: Eve gets a hold of Bobs E-Mail account, and finds the email Alice sent
    Eve->>EveBrowser: Opens the link

    activate EveBrowser
    Eve->>EveBrowser: Enters password
    EveBrowser->>Server: Loads payload for ID<br/>without sending the password
    Server->>BobBrowser: Responds no payload
    EveBrowser->>Eve: Displays an error
    deactivate EveBrowser
```
