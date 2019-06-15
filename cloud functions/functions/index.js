// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
// var serviceAccount = require('C:/Users/Nachiketa/Desktop/myproject2/serviceAccountKey.json')
// admin.initializeApp(
//     {
//         credential: admin.credential.cert(serviceAccount),
//         databaseURL: "https://lifesync-3fbba.firebaseio.com/"
//     }
// );

admin.initializeApp(functions.config().firebase);




exports.onSosCreate = functions.database
.ref('/SOS/{SOSID}')
.onCreate((snapshot,context)=>{
    const sosID = context.params.SOSID;
    console.log(`New SOS generated ${sosID}`);
    const sosData = snapshot.val();
    const toID = sosData.to_ID;
    const fromID = sosData.from_ID;
    const fromType = sosData.from_TYPE;
    const toType = sosData.to_TYPE;
    const msg = sosData.message;
    console.log(`  ${fromID} ${toID}  ${fromType} ${toType} ${msg} `);

    var db = admin.database();
    var ref = db.ref(`/USERS/${toID}`);
    var token;
    return ref.once('value',function(data){
        token = data.val().token;
        console.log(token);
    }).then(function(data){
        console.log(`inside once :${sosID}`);
        var payload = {
            data :{
                "title" : "You received an SOS",
                "body" : "Message :"+msg,
                "msgId" :sosID,
                "msg_type" : "sos"
            }
        };
    
        return admin.messaging().sendToDevice(token, payload)
                            .then(function(response) {
                                console.log("Successfully sent message:", response);
                                return null;
                              })
                              .catch(function(error) {
                                console.log("Error sending message:", error);
                              });  
    }).catch(function(e){
        console.log('error occurred');
        });

});

exports.requestAppUsageState = functions.https.onCall((data, context) => {
    console.log("Requesting Child");
    var token = data.token;
    var from_userId = data.from_userId;
    var payload = {
        data :{
            "from_userId" : from_userId,
            "msg_type" : "appUsageRequest"
        }
    };

    return admin.messaging().sendToDevice(token, payload)
						.then(function(response) {
                            console.log("Request Sent Successfully:", response);
                            return null;
						  })
						  .catch(function(error) {
							console.log("Error sending message:", error);
						  });
  });

  exports.sendAppUsageState = functions.https.onCall((data, context) => {
    console.log("incomming appList");
    var token = data.token;
    var from_userId = data.from_userId;
    var appNames = JSON.stringify(data.appNames);
    var appPackageNames = JSON.stringify(data.appPackageNames);
    var appTimeUsages = JSON.stringify(data.appTimeUsages);
    // var appIcons = data.appIcons;
    console.log("appNames");
    console.log(appNames);
    console.log("appPackageNames");
    console.log(appPackageNames);
    console.log("appTime");
    console.log(appTimeUsages);

    var payload = {
        data :{
            "from_userId" : from_userId,
            "msg_type" : "incomming_appDetails",
            "appNames" : appNames,
            "appPackageNames" : appPackageNames,
            "appTimeUsages" : appTimeUsages
        }
    };

   
    return admin.messaging().sendToDevice(token, payload)
						.then(function(response) {
                            console.log("AppDetails Sent Successfully:", response);
                            return null;
						  })
						  .catch(function(error) {
							console.log("Error sending AppList:", error);
						  });
  });
  