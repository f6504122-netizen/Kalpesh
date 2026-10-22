// firebase.js - Firebase v9+ Modular SDK Configuration for KevSpace
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

// Your web app's Firebase configuration
// In production, these environment variables are populated from .env / Secrets panel
const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY || "AIzaSyDummyKeyForKevSpaceRealtimeMessenger123",
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN || "kevspace-messenger.firebaseapp.com",
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID || "kevspace-messenger",
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET || "kevspace-messenger.appspot.com",
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID || "102938475610",
  appId: process.env.REACT_APP_FIREBASE_APP_ID || "1:102938475610:web:abcdef1234567890"
};

// Initialize Firebase App
const app = initializeApp(firebaseConfig);

// Initialize Firebase Authentication
export const auth = getAuth(app);

// Initialize Google Auth Provider with custom parameters
export const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({
  prompt: "select_account"
});

// Initialize Cloud Firestore Database
export const db = getFirestore(app);

// Export Auth & Firestore utility functions for clean component integration
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
