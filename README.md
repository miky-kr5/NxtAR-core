NxtAR: A generic software architecture for Augmented Reality based mobile robot control.
========================================================================================

Core module
-----------

### Abstract ###

NxtAR is a generic software architecture for the development of Augmented Reality games
and applications centered around mobile robot control. This is a reference implementation
with support for [LEGO Mindstorms NXT][1] mobile robots.

### Module description ###

The core module comprises all the operating system independent classes that implemente the
base architecture and the different scenarios for the application. This implementation is
designed and built around the [LibGDX][2] and the [Artemis Entity-System Framework][3] libraries.

Currently there is one scenario titled *Bomb Game*.

### Module installation and usage. ###

The core module cannot be used directly. It is intended to be compiled with a LibGDX backend module.

 [1]: http://www.lego.com/en-us/mindstorms/?domainredir=mindstorms.lego.com
 [2]: http://libgdx.badlogicgames.com/
 [3]: http://gamadu.com/artemis/
