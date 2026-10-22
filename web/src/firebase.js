// web/src/firebase.js - Firebase v9+ Modular SDK Configuration for KevSpace
import { initializeApp } from "firebase/app";
import { 
  getAuth, 
  GoogleAuthProvider, 
  signInWithPopup, 
  signOut as firebaseSignOut 
} from "firebase/auth";
import { 
  getFirestore, 
  collection, 
  doc, 
  setDoc, 
  getDoc, 
  getDocs, 
  addDoc, 
  query, 
  where, 
  orderBy, 
  onSnapshot, 
  serverTimestamp 
} from "firebase/firestore";

const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY || "AIzaSyDummyKeyForKevSpaceRealtimeMessenger123",
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN || "kevspace-messenger.firebaseapp.com",
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID || "kevspace-messenger",
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET || "kevspace-messenger.appspot.com",
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID || "102938475610",
  appId: process.env.REACT_APP_FIREBASE_APP_ID || "1:102938475610:web:abcdef1234567890"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({
  prompt: "select_account"
});
export const db = getFirestore(app);

export {
  signInWithPopup,
  firebaseSignOut,
  collection,
  doc,
  setDoc,
  getDoc,
  getDocs,
  addDoc,
  query,
  where,
  orderBy,
  onSnapshot,
  serverTimestamp
};

export default app;
