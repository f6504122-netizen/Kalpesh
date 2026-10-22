import React, { useState, useEffect, useRef } from "react";
import { 
  db, 
  collection, 
  doc, 
  setDoc, 
  addDoc, 
  query, 
  orderBy, 
  onSnapshot, 
  serverTimestamp 
} from "../firebase";

export default function ChatWindow({ currentUser, recipient }) {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const messagesEndRef = useRef(null);

  // Deterministic chat ID based on participant UIDs so both users join the same conversation
  const chatId = [currentUser.uid, recipient.id || recipient.uid].sort().join("_");

  // Real-time message feed listener for the active chat channel
  useEffect(() => {
    if (!currentUser || !recipient) return;

    setLoading(true);
    // Reference to /chats/{chatId}/messages
    const messagesRef = collection(db, "chats", chatId, "messages");
    const q = query(messagesRef, orderBy("timestamp", "asc"));

    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        const fetchedMessages = [];
        snapshot.forEach((docSnap) => {
          fetchedMessages.push({
            id: docSnap.id,
            ...docSnap.data()
          });
        });
        setMessages(fetchedMessages);
        setLoading(false);
      },
      (error) => {
        console.error("Firestore messages onSnapshot listener error:", error);
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, [chatId, currentUser, recipient]);

  // Smooth scroll to latest message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // Send message directly to Firestore
  const handleSendMessage = async (e) => {
    e.preventDefault();
    const trimmedMessage = newMessage.trim();
    if (!trimmedMessage) return;

    // Reset input immediately for responsive UX
    setNewMessage("");

    try {
      const recipientUid = recipient.id || recipient.uid;

      // 1. Ensure the parent chat document exists and stores participants
      const chatDocRef = doc(db, "chats", chatId);
      await setDoc(
        chatDocRef,
        {
          chatId,
          participants: [currentUser.uid, recipientUid],
          lastMessage: trimmedMessage,
          lastMessageTimestamp: serverTimestamp(),
          lastSenderId: currentUser.uid,
          updatedAt: serverTimestamp()
        },
        { merge: true }
      );

      // 2. Append message to the subcollection /chats/{chatId}/messages
      const messagesCollectionRef = collection(db, "chats", chatId, "messages");
      await addDoc(messagesCollectionRef, {
        chatId,
        senderId: currentUser.uid,
        senderName: currentUser.displayName || "User",
        senderPhoto: currentUser.photoURL || "",
        recipientId: recipientUid,
        text: trimmedMessage,
        timestamp: serverTimestamp()
      });
    } catch (err) {
      console.error("Error writing message to Firestore:", err);
    }
  };

  const formatTimestamp = (timestamp) => {
    if (!timestamp) return "Just now";
    try {
      const date = timestamp.toDate ? timestamp.toDate() : new Date(timestamp);
      return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    } catch {
      return "Just now";
    }
  };

  return (
    <div className="flex-1 h-full flex flex-col bg-slate-950 text-slate-100 overflow-hidden font-sans">
      {/* 1. Header with Selected User's Profile & Status */}
      <header className="px-6 py-4 bg-slate-900 border-b border-slate-800 flex items-center justify-between shadow-sm z-10">
        <div className="flex items-center gap-3.5">
          <div className="relative">
            <img
              src={
                recipient.photoURL ||
                `https://ui-avatars.com/api/?name=${encodeURIComponent(recipient.displayName || "User")}&background=6366F1&color=fff`
              }
              alt={recipient.displayName}
              className="w-11 h-11 rounded-full object-cover ring-2 ring-indigo-500/40"
            />
            <span
              className={`absolute bottom-0 right-0 w-3.5 h-3.5 rounded-full border-2 border-slate-900 ${
                recipient.status === "online" ? "bg-emerald-500" : "bg-slate-500"
              }`}
            />
          </div>

          <div>
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <span>{recipient.displayName || "User"}</span>
            </h3>
            <p className="text-xs text-slate-400 flex items-center gap-1.5">
              <span
                className={`w-1.5 h-1.5 rounded-full ${
                  recipient.status === "online" ? "bg-emerald-400 animate-pulse" : "bg-slate-400"
                }`}
              />
              <span>{recipient.status === "online" ? "Active Now" : "Offline"}</span>
              <span>•</span>
              <span className="truncate max-w-[200px] sm:max-w-xs">{recipient.email}</span>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <span className="hidden sm:inline-flex items-center gap-1 text-xs text-indigo-400 bg-indigo-950/60 border border-indigo-800/60 px-3 py-1 rounded-full">
            <span className="w-2 h-2 rounded-full bg-cyan-400 animate-ping" />
            Live Channel
          </span>
        </div>
      </header>

      {/* 2. Real-time Message Feed */}
      <div className="flex-1 overflow-y-auto px-4 sm:px-6 py-6 space-y-4">
        {loading ? (
          <div className="h-full flex flex-col items-center justify-center text-slate-500 text-sm gap-3">
            <div className="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin" />
            <p>Syncing KevSpace conversation with Firestore...</p>
          </div>
        ) : messages.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center p-8 text-slate-400 max-w-md mx-auto">
            <div className="w-16 h-16 rounded-2xl bg-indigo-950/80 border border-indigo-500/30 flex items-center justify-center mb-4 text-cyan-400 shadow-lg shadow-indigo-950">
              <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
            </div>
            <h4 className="text-lg font-bold text-white mb-1">
              Start chatting with {recipient.displayName}
            </h4>
            <p className="text-sm text-slate-400 leading-relaxed">
              Every message sent here writes directly to Cloud Firestore with server timestamps and syncs in real-time.
            </p>
          </div>
        ) : (
          messages.map((msg) => {
            const isMine = msg.senderId === currentUser.uid;

            return (
              <div
                key={msg.id}
                className={`flex flex-col ${isMine ? "items-end" : "items-start"}`}
              >
                <div className="flex items-end gap-2 max-w-[85%] sm:max-w-[70%]">
                  {!isMine && (
                    <img
                      src={
                        msg.senderPhoto ||
                        `https://ui-avatars.com/api/?name=${encodeURIComponent(msg.senderName || "User")}&background=6366F1&color=fff`
                      }
                      alt={msg.senderName}
                      className="w-7 h-7 rounded-full object-cover mb-1 flex-shrink-0"
                    />
                  )}

                  <div
                    className={`rounded-2xl px-4 py-2.5 text-sm sm:text-base leading-relaxed break-words shadow-md ${
                      isMine
                        ? "bg-gradient-to-r from-indigo-600 to-indigo-700 text-white rounded-br-none"
                        : "bg-slate-800 border border-slate-700/80 text-slate-100 rounded-bl-none"
                    }`}
                  >
                    {!isMine && (
                      <p className="text-[11px] font-bold text-cyan-400 mb-0.5">
                        {msg.senderName}
                      </p>
                    )}
                    <p>{msg.text}</p>
                    <div
                      className={`text-[10px] mt-1 flex items-center justify-end gap-1 ${
                        isMine ? "text-indigo-200" : "text-slate-400"
                      }`}
                    >
                      <span>{formatTimestamp(msg.timestamp)}</span>
                      {isMine && <span>✓✓</span>}
                    </div>
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* 3. Message Input Bar with Send Action */}
      <footer className="p-4 bg-slate-900 border-t border-slate-800">
        <form onSubmit={handleSendMessage} className="flex items-center gap-2 max-w-4xl mx-auto">
          <input
            type="text"
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder={`Message ${recipient.displayName || "user"}...`}
            className="flex-1 bg-slate-950 text-slate-100 px-4 py-3 rounded-xl border border-slate-800 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 placeholder-slate-500 text-sm sm:text-base transition"
          />

          <button
            type="submit"
            disabled={!newMessage.trim()}
            className="bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-800 disabled:text-slate-600 text-white font-medium p-3 rounded-xl transition duration-150 flex items-center justify-center flex-shrink-0 cursor-pointer disabled:cursor-not-allowed shadow-md shadow-indigo-600/30"
          >
            <svg className="w-5 h-5 rotate-90" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          </button>
        </form>
      </footer>
    </div>
  );
}
