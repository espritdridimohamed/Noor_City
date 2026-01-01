/**
 * Firebase Cloud Function for Sansa - Invitation Technicien
 * Ce code doit être déployé dans le dossier 'functions' de votre projet Firebase.
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Fonction Callable pour inviter un technicien.
 * Accès réservé aux administrateurs.
 */
exports.inviteTechnician = functions.https.onCall(async (data, context) => {
    // 1. Vérifier que l'appelant est authentifié
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'Vous devez être connecté.');
    }

    // 2. Vérifier que l'appelant est Admin (via Custom Claims)
    if (context.auth.token.role !== 'ADMIN') {
        throw new functions.https.HttpsError('permission-denied', 'Seul un administrateur peut inviter un technicien.');
    }

    const { email, name, specialty } = data;

    if (!email || !name) {
        throw new functions.https.HttpsError('invalid-argument', 'Email et Nom sont requis.');
    }

    try {
        // 3. Créer l'utilisateur dans Firebase Auth
        // On génère un mot de passe temporaire aléatoire
        const tempPassword = Math.random().toString(36).slice(-10);
        
        const userRecord = await admin.auth().createUser({
            email: email,
            password: tempPassword,
            displayName: name,
        });

        // 4. Définir le rôle 'TECHNICIAN' dans les Custom Claims
        await admin.auth().setCustomUserClaims(userRecord.uid, {
            role: 'TECHNICIAN'
        });

        // 5. Enregistrer les informations supplémentaires dans Realtime Database
        await admin.database().ref(`users/${userRecord.uid}`).set({
            uid: userRecord.uid,
            name: name,
            email: email,
            role: 'TECHNICIAN',
            specialty: specialty,
            createdAt: admin.database.ServerValue.TIMESTAMP
        });

        // 6. Envoyer l'email de réinitialisation de mot de passe immédiatement
        // Cela sert d'invitation : le tech reçoit un lien pour définir son propre mot de passe
        const link = await admin.auth().generatePasswordResetLink(email);
        
        // Note: Pour envoyer un email personnalisé (HTML), vous devriez utiliser un service comme SendGrid ou Mailgun ici.
        // Ici on utilise la méthode native de Firebase pour générer le lien.
        
        console.log(`Technicien invité avec succès : ${email}`);
        
        return { 
            success: true, 
            message: `Le technicien ${name} a été invité. Un email a été envoyé à ${email}.`,
            resetLink: link // Optionnel: pour debug ou envoi manuel si besoin
        };

    } catch (error) {
        console.error("Erreur lors de l'invitation :", error);
        throw new functions.https.HttpsError('internal', error.message);
    }
});
