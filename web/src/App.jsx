import React, { useState, useEffect } from "react";
import { auth, onSnapshot, doc, db } from "./firebase";
import { onAuthStateChanged } from "firebase/auth";
import Login from "./components/Login";
import Sidebar from "./components/Sidebar";
import ChatWindow from "./components/ChatWindow";

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [activeRecipient, setActiveRecipient] = useState(null);
  const [initializing, setInitializing] = useState(true);

  // Monitor Firebase Authentication state
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      if (user) {
        setCurrentUser(user);
      } else {
        setCurrentUser(null);
        setActiveRecipient(null);
      }
      setInitializing(false);
    });

    return () => unsubscribe();
  }, []);

  if (initializing) {
    return (
      <div className="min-h-screen w-full flex items-center justify-center bg-slate-950 text-indigo-400">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
          <p className="text-sm font-semibold tracking-wide text-slate-300">Initializing KevSpace...</p>
        </div>
      </div>
    );
  }

  // If not signed in, show the Login Component
  if (!currentUser) {
    return <Login onUserAuthenticated={(user) => setCurrentUser(user)} />;
  }

  // When signed in, show KevSpace Workspace (Sidebar + Chat Window)
  return (
    <div className="h-screen w-screen flex flex-col md:flex-row overflow-hidden bg-slate-950 font-sans">
      <Sidebar
        currentUser={currentUser}
        activeRecipient={activeRecipient}
        onSelectRecipient={(recipient) => setActiveRecipient(recipient)}
      />

      <main className="flex-1 h-full flex flex-col">
        {activeRecipient ? (
          <ChatWindow currentUser={currentUser} recipient={activeRecipient} />
        ) : (
          <div className="flex-1 h-full flex flex-col items-center justify-center text-center p-8 bg-slate-950 text-slate-400">
            <div className="w-20 h-20 rounded-3xl bg-indigo-950/60 border border-indigo-500/20 flex items-center justify-center mb-6 text-indigo-400 shadow-xl shadow-indigo-950/50">
              <svg className="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 8h2a2 2 0 012 2v6a2 2 0 01-2 2h-2v4l-4-4H9a1.994 1.994 0 01-1.414-.586m0 0L11 14h4a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2v4l.586-.586z" />
              </svg>
            </div>
            <h3 className="text-2xl font-bold text-white mb-2">Welcome to KevSpace</h3>
            <p className="max-w-md text-sm sm:text-base text-slate-400 leading-relaxed mb-6">
              Select any active user from the sidebar to open a real-time messaging channel backed by Cloud Firestore.
            </p>
            <div className="flex items-center gap-2 text-xs text-indigo-400 bg-indigo-950/50 px-4 py-2 rounded-full border border-indigo-800/40">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              Connected as {currentUser.displayName || currentUser.email}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
