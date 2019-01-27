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
                   /*      
                    notification: {
                        click_action: "MAIN",
                        title: `New Chat Request`,
                        body: `${senderUsername} sent you a new chat request.`,
                        icon: `default`
                    }, */

                    data: {
                        "type": "Request",
                        "contact": sender,
                        "click_action": "MAIN",
                        "title": `New Chat Request`,
                        "body": `${senderUsername} sent you a new chat request.`,
                        "icon": `default`
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
                        
                  /*   notification: {
                        title: `Contact Removed`,
                        body: `${senderUsername} removed you from contacts.`,
                        icon: `default`
                    },
 */
                    data: {
                        "type": "remove",
                        "contact": sender,
                        "title": `Contact Removed`,
                        "body": `${senderUsername} removed you from contacts.`,
                        "icon": `default`
                    }
                }

                return admin.messaging().sendToDevice(receiverToken, contactRemovedNotification);
            })
            .then(result => {
                console.log("Notficcation sended to device.");
            })
            .catch(error => console.log(error));

    });


exports.sendNewGroupMessageNotification = functions.database.ref('/GroupChatMessages/{groupId}/{messageId}')
    .onCreate((data, context)  => {

        const groupId = context.params.groupId;
        const messageId = context.params.messageId;

        const requestMessageCall = admin.database().ref(`/GroupChatMessages/${groupId}/${messageId}`).once('value');
        const requestGroupMembers = admin.database().ref(`/GroupMembers`).orderByChild(groupId).once('value');
        const requestGroupName = admin.database().ref(`/Groups/${groupId}/group_name`).once('value');

        let groupMembers;
        let message;
        let groupName;

        return requestMessageCall
            .then(result => {
                message = result.val();
                console.log("*******************************************************************************");
                console.log( "message" );
                console.log( message );
                console.log("*******************************************************************************");
                return requestGroupName;
            })
            .then(result => {
                groupName = result.val();
                console.log("*******************************************************************************");
                console.log( "groupName" );
                console.log( groupName );
                console.log("*******************************************************************************");
                return requestGroupMembers;
            })
            .then(resultMembers => {
                groupMembers = resultMembers.val();

                console.log("*******************************************************************************");
                console.log( "groupMembers" );
                console.log( groupMembers );
                console.log(typeof groupMembers);
                console.log("*******************************************************************************");



                const groupMembersMap = Object.keys(groupMembers);
                

                for (const member of groupMembersMap) {

                    const requestMemberTokenCall = admin.database().ref(`/Users/${member}/device_token`).once('value');
                
                    requestMemberTokenCall
                        .then(resultToken =>  {
                            const token = resultToken.val();
                            const notificationType = "new_group_message";

                            const newGroupMessageNotification = {
                                
                             /*    notification: {
                                    click_action: "MAIN",
                                    title: `New Group Message`,
                                    body: `${message.message_owner}: ${message.message_content}`,
                                    icon: `default`
                                }, */
        
                                data: {
                                    "type": notificationType,
                                    "groupId": groupId,
                                    "click_action": "MAIN",
                                    "title": `New Group Message`,
                                    "body": `${message.message_owner}: ${message.message_content}`,
                                    "icon": `default`
                                }
                            }

                            admin.messaging().sendToDevice(token, newGroupMessageNotification)
                                .then(result => {
                                    console.log("*******************************************************************************");
                                    console.log("New Group Message Notification sended to device.");
                                    console.log("*******************************************************************************");
                                })
                        })
                }
            })
        .catch(error => console.log(error));

    });


exports.sendNewGroupNotification = functions.database.ref('/GroupMembers/{memberId}/{groupId}/')
    .onCreate((data, context) => {

        const memberId  = context.params.memberId;
        const groupId  = context.params.groupId;

        console.log("**************************************************************************************");
        console.log("MEMBER: " + memberId);
        console.log("GROUP: " + groupId);

        const requestMemberTokenCall = admin.database().ref(`/Users/${memberId}/device_token`).once('value');
        const requestGroupNameCall = admin.database().ref(`/Groups/${groupId}/group_name`).once('value');

        let memberToken;
        let groupName;

        return requestMemberTokenCall
            .then(result => {
                memberToken = result.val();
                return requestGroupNameCall;
            })
            .then(result => {
                groupName = result.val();

                const notificationType = "new_group";
                console.log("NOTIFICATION TYPE: " + notificationType);
                console.log( typeof notificationType );
                console.log("**************************************************************************************");

                const newGroupNotification = {
                        
                   /*  notification: {
                        click_action: "MAIN",
                        title: `New Group Created`,
                        body: `${groupName}`,
                        icon: `default`
                    }, */

                    data: {
                        "type": notificationType,
                        "groupId": groupId,
                        "click_action": "MAIN",
                        "title": `New Group Created`,
                        "body": `${groupName}`,
                        "icon": `default`
                    }
                }

                return admin.messaging().sendToDevice(memberToken, newGroupNotification); 
            })
            .then(result => {
                console.log("New Group Notification sended to device.");
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

        let receiverToken;
        let senderUsername;
        let messageOwner;

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
                            
                        /* notification: {
                            click_action: "MAIN",
                            title: `New Message`,
                            body: `${senderUsername} sent you a new message.`,
                            icon: `default`
                        }, */
    
                        data: {
                            "type": notificationType,
                            "contact": messageOwner,
                            "click_action": "MAIN",
                            "title": `New Message`,
                            "body": `${senderUsername} sent you a new message.`,
                            "icon": `default`
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