# roomba-webcam-android
=====================

This was CIS 542 Embedded Systems course final project in Spring 2012. The project was done together with John Mayer.

## Technical Documentation 
============================
 The system consists of three main components: the android application,
 the command service, and the video service. 
 
### Android 
--------------

An android app was designed and implemented to display real-time video
and take user commands to remotely control the drive of a roomba over
an IP network. Once configured with a remote address and respective
service port numbers, persistent sockets are opened to each
service. Three main asynchronous tasks are used to coordination
network communication.

* SenderSocketOpenerTask 

This task simply opens a network socket and updates the global
reference `senderSocket`. Any attempts to use `senderSocket` before it
is ready will fail, but this is handled and will be explained in the
next section. A single SenderSocketOpenerTask is started with the
pertinent arguments when the user enters the network address
information.

* SendCommandTask. 

A new instance of SendCommandTask is created and executed each time a
command needs to be sent to the command service. Each SendCommandTask
uses the same socket instance - `senderSocket`. If `senderSocket` is
null we simply exit the task, assuming that sometime in the future the
socket will be ready for use (or something has gone wrong with the
server). This approach was significantly simpler than synchronizing
between the opening and sending tasks.

* GetVideoTask 

This final task implements the video communication protocol. A row
synchronization byte is sent to the video service, and the task waits
for a row of RGB pixel data from the video service. This handshake is
repeated ad infinitum, with a looping counter that determines which
row we are on. As rows are received, a bitmap is updated, and a
progress update is reported to the UI thread. The progress callback
simply updates the screen with the most recent version of the bitmap
in memory.
  
### Command Service 
----------------------

The command service is a standalone UNIX process. At the start, a
serial port is determined and a roomba_object is initialized. This
action opens the serial port and readies the process to accept
commands. A tcp server socket is bound, and waits for connections
on a port number, also determined at the command line.

The command service performs a simple translation from command
characters sent over the network to a different set of byte commands
sent down a serial port. Using the open source roomba library, we have
access to a set of commands such as `roomba_forward`, which, for
example, sends 3 bytes to a serial port we define at program
start. The following table describes the translation:

<table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">
<colgroup><col class="left" /><col class="left" />
</colgroup>
<tbody>
<tr><td class="left">TCP character</td><td class="left">Libary call</td></tr>
<tr><td class="left">---------------</td><td class="left">-----------------</td></tr>
<tr><td class="left">w</td><td class="left">roomba<sub>forward</sub></td></tr>
<tr><td class="left">a</td><td class="left">roomba<sub>left</sub></td></tr>
<tr><td class="left">s</td><td class="left">roomba<sub>right</sub></td></tr>
<tr><td class="left">d</td><td class="left">roomba<sub>backward</sub></td></tr>
<tr><td class="left">p</td><td class="left">roomba<sub>stop</sub></td></tr>
</tbody>
</table>


### Video Service 
--------------------

The video service is also a standalone UNIX process. It
immediately binds a tcp server socket and waits for a
connection. The process then reads a frame of video from the
kinect, using the libfreekinect open source library. Then, for
each row in the frame, the video service waits for a video
synchronization byte before sending a single row of RGB pixel
data. When there are no more rows from the frame, the loop
continues. In order to boost framerate, the resolution was
downsampled such that only every 1/4 rows and 1/4 columns are
actually transmitted to the client. Since the bottleneck seemed to
be the network, it may have been possible to implement some
rudimentary blending on the server by taking the average of the
4x4 pixel region.


## User Documentation 
=======================
   
### Roomba 
-------------

Make sure the roomba has network connectivity. This can be done
however is most convient, but we chose to simply connect the
beagleboard to a stock wireless router via ethernet.

Next, connect the Microsoft connect to the beagleboard via USB,
and similarly connect the roomba via USB-serial link to the
beagleboard. Finally, ensure that all devices are powered. In our
demo we used a standard wall outlet, but it would be easy to add
voltage regulators and power all devices concurrently off of the
roomba battery. Boot up the beagleboard (Ubuntu)

Using screen, tmux, or unix job management (& to background task),
run the following commands:

    com_server /dev/ttyUSB0 8001

This starts a command service process, attached to the serial port
at /dev/ttyUSB0, and bound to the tcp port 8001

    vid_server /dev/null 8002

This starts a video service process, bound to the tcp port 8001

At this point, make note of the IP address of the beagleboard. The
back-end system is now ready to accept a client connection. In
this example, we will use the address "192.168.1.2"

### Android 
--------------

Start the application. At the first screen, enter the following
information: 

    IP Address 192.168.1.2  
    Command Port  8001  
    Video Port8002  

And hit Submit.

The next screen will begin showing the real-time video feed. At
the bottom, there are 5 buttons that can move the roomba forward
or backward, turn the roomba in-place in either direction, or stop
the roomba drive. The quit button can also be used to terminate
the connection and return to the first screen.

In our most recent version, you can tilt the android, and the
accelerometer will interpret the current position as a command.

