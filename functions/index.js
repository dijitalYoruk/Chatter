const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendRequestNotification = functions.database.ref('/ChatRequests/{receiver}/{sender}')
    .onCreate( (data, context) => {

        const receiver = context.params.receiver;
        const sender   = context.params.sender;

        const requestReceiverTokenCall    = admin.database().ref(`/Users/${receiver}/device_token`).once('value');
        const requestSenderUsernameCall   = admin.database().ref(`/Users/${sender}/username`).once('value');
        
        let receiverToken;
        let senderUsername;

        return requestReceiverTokenCall
            .then(result => {
                receiverToken = result.val();
                console.log( "RECEIVER TOKEN: " + receiverToken );
                console.log( typeof receiverToken );
                return requestSenderUsernameCall
            })
            .then(result => {
                senderUsername = result.val();
                console.log("SENDER USERNAME: " + senderUsername);
                console.log( typeof senderUsername );

                const chatRequestNotification = {
                        
                    notification: {
                        click_action: "MAIN",
                        title: `New Chat Request`,
                        body: `${senderUsername} sent you a new chat request.`,
                        icon: `default`
                    },

                    data: {
                        "type": "Request",
                        "contact": sender
                    }
                }

                return admin.messaging().sendToDevice(receiverToken, chatRequestNotification);
            })
            .then(result => {
                console.log("Notficcation sended to device.");
            })
            .catch(error => console.log(error));

    });


    exports.sendContactRemovedNotification = functions.database.ref('/Contacts/{receiver}/{sender}')
    .onDelete((data, context) => {

        const receiver = context.params.receiver;
        const sender   = context.params.sender;

        const requestReceiverTokenCall    = admin.database().ref(`/Users/${receiver}/device_token`).once('value');
        const requestSenderUsernameCall   = admin.database().ref(`/Users/${sender}/username`).once('value');
        
        let receiverToken;
        let senderUsername;

        return requestReceiverTokenCall
            .then(result => {
                receiverToken = result.val();
                console.log( "RECEIVER TOKEN: " + receiverToken );
                console.log( typeof receiverToken );
                return requestSenderUsernameCall
            })
            .then(result => {
                senderUsername = result.val();
                console.log("SENDER USERNAME: " + senderUsername);
                console.log( typeof senderUsername );

                const contactRemovedNotification = {
                        
                    notification: {
                        title: `Contact Removed`,
                        body: `${senderUsername} removed you from contacts.`,
                        icon: `default`
                    },

                    data: {
                        "type": "remove",
                        "contact": sender
                    }
                }

                return admin.messaging().sendToDevice(receiverToken, contactRemovedNotification);
            })
            .then(result => {
                console.log("Notficcation sended to device.");
            })
            .catch(error => console.log(error));

    });

exports.sendNewMessageNotification = functions.database.ref('/Chats/{sender}/{receiver}/{messageId}')
    .onCreate((data, context) => {

        const receiver  = context.params.receiver;
        const sender    = context.params.sender;
        const messageId = context.params.messageId;
        console.log("**************************************************************************************");
        console.log("RECEIVER: " + receiver);
        console.log("SENDER: " + sender);
        console.log("MESSAGE ID" + messageId);

        const requestReceiverTokenCall    = admin.database().ref(`/Users/${receiver}/device_token`).once('value');
        const requestSenderUsernameCall   = admin.database().ref(`/Users/${sender}/username`).once('value');
        const requestMessageOwnerCall = admin.database().ref(`/Chats/${sender}/${receiver}/${messageId}/message_owner`).once('value');


        return requestReceiverTokenCall
            .then(result => {
                receiverToken = result.val();
                console.log( "RECEIVER TOKEN: " + receiverToken );
                console.log( typeof receiverToken );
                return requestSenderUsernameCall
            })
            .then(result => {
                senderUsername = result.val();
                console.log("SENDER USERNAME: " + senderUsername);
                console.log( typeof senderUsername );
                return requestMessageOwnerCall;
            })
            .then(result => {
                messageOwner = result.val();

                console.log("MESSAGE OWNER: " + sender);
                console.log("SENDER: " + messageOwner);

                if (messageOwner === sender) {
                    const notificationType = "new_message";
                    console.log("NOTIFICATION TYPE: " + notificationType);
                    console.log( typeof notificationType );
                    console.log("**************************************************************************************");

                    const newMessageNotification = {
                            
                        notification: {
                            click_action: "MAIN",
                            title: `New Message`,
                            body: `${senderUsername} sent you a new message.`,
                            icon: `default`
                        },
    
                        data: {
                            "type": notificationType,
                            "contact": messageOwner
                        }
                    }
    
                    return admin.messaging().sendToDevice(receiverToken, newMessageNotification); 
                }
                           
            })
            .then(result => {
                console.log("New Message Notification sended to device.");
            })
            .catch(error => console.log(error));

    });