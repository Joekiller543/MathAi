import React, { useState, useRef, useEffect } from 'react';
import { ChatMessage, VirtualFile, ChatAttachment } from '@/types';
import { Send, Bot, User, Globe, Sparkles, Loader2, Trash2, Paperclip, X, File as FileIcon } from 'lucide-react';
import { chatWithBot, generateAppStructure, searchWeb } from '../services/geminiService';

interface ChatInterfaceProps {
  files: VirtualFile[];
  onFilesGenerated: (newFiles: VirtualFile[]) => void;
}

const STORAGE_KEY = 'devforge_chat_history';

export const ChatInterface: React.FC<ChatInterfaceProps> = ({ files, onFilesGenerated }) => {
  const [messages, setMessages] = useState<ChatMessage[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    return saved ? JSON.parse(saved) : [{ id: '1', role: 'model', text: 'Hello! I am DevForge AI.', timestamp: Date.now() }];
  });
  
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [mode, setMode] = useState<'chat' | 'build' | 'search'>('chat');
  const [attachments, setAttachments] = useState<ChatAttachment[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
        const file = e.target.files[0];
        const reader = new FileReader();
        reader.onload = (evt) => {
            const result = evt.target?.result as string;
            if (result) {
                setAttachments(prev => [...prev, { name: file.name, type: file.type, data: result }]);
            }
        };
        reader.readAsDataURL(file);
    }
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleSend = async () => {
    if ((!input.trim() && attachments.length === 0) || isLoading) return;
    const userText = input;
    const currentAttachments = [...attachments];
    setMessages(prev => [...prev, { id: Date.now().toString(), role: 'user', text: userText, timestamp: Date.now(), attachments: currentAttachments }]);
    setInput('');
    setAttachments([]);
    setIsLoading(true);

    try {
      let responseText = '';
      let grounding: string[] | undefined = undefined;

      if (mode === 'build') {
        let keepGoing = true;
        let iteration = 1;
        let currentPrompt = userText;
        let localFiles = [...files]; 
        setMessages(prev => [...prev, { id: 'building', role: 'model', text: 'Thinking & Building...', timestamp: Date.now() }]);

        while (keepGoing && iteration <= 5) { 
            const result = await generateAppStructure(currentPrompt, localFiles);
            const newLocalFiles = [...localFiles];
            result.files.forEach(gf => {
                const idx = newLocalFiles.findIndex(f => f.name === gf.name);
                if (idx >= 0) newLocalFiles[idx] = gf; else newLocalFiles.push(gf);
            });
            localFiles = newLocalFiles;
            onFilesGenerated(result.files);
            setMessages(prev => [...prev, { id: Date.now().toString() + iteration, role: 'model', text: `**Step ${iteration}:** ${result.reasoning}`, timestamp: Date.now() }]);
            if (result.status === 'COMPLETE') keepGoing = false;
            else { iteration++; currentPrompt = "Continue building."; }
        }
      } else if (mode === 'search') {
         const res = await searchWeb(userText);
         responseText = res.text;
         grounding = res.sources;
         setMessages(prev => [...prev, { id: Date.now().toString(), role: 'model', text: responseText, timestamp: Date.now(), groundingMetadata: grounding }]);
      } else {
        const history = messages.map(m => ({ role: m.role, parts: [{ text: m.text }, ...(m.attachments?.map(a => ({ inlineData: { mimeType: a.type, data: a.data.split(',')[1] } })) || [])] }));
        responseText = await chatWithBot(history, userText, currentAttachments, files);
        setMessages(prev => [...prev, { id: Date.now().toString(), role: 'model', text: responseText, timestamp: Date.now() }]);
      }
    } catch (error) {
      setMessages(prev => [...prev, { id: Date.now().toString(), role: 'model', text: "Error.", timestamp: Date.now(), isError: true }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full bg-gray-900">
      <input type="file" ref={fileInputRef} className="hidden" onChange={handleFileSelect} />
      <div className="p-4 border-b border-gray-800 bg-gray-800/50 flex justify-between items-center">
         <div className="flex gap-2 p-1 bg-gray-900 rounded-lg">
            <button onClick={() => setMode('chat')} className={`px-3 py-1 text-xs rounded ${mode === 'chat' ? 'bg-indigo-600' : 'text-gray-400'}`}>Chat</button>
            <button onClick={() => setMode('build')} className={`px-3 py-1 text-xs rounded ${mode === 'build' ? 'bg-purple-600' : 'text-gray-400'}`}>Build</button>
            <button onClick={() => setMode('search')} className={`px-3 py-1 text-xs rounded ${mode === 'search' ? 'bg-emerald-600' : 'text-gray-400'}`}>Search</button>
         </div>
         <button onClick={() => setMessages([])} className="text-gray-500"><Trash2 size={16} /></button>
      </div>
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((msg) => (
          <div key={msg.id} className={`flex gap-3 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            {msg.role === 'model' && <Bot size={16} className="text-indigo-500 mt-2" />}
            <div className={`max-w-[85%] rounded-2xl px-4 py-3 text-sm ${msg.role === 'user' ? 'bg-indigo-600' : 'bg-gray-800'}`}>
              {msg.attachments?.map((att, i) => <div key={i} className="mb-2 text-xs bg-black/20 p-1 rounded">{att.name}</div>)}
              <div className="whitespace-pre-wrap">{msg.text}</div>
              {msg.groundingMetadata?.map((url, i) => <a key={i} href={url} target="_blank" className="block text-xs text-blue-400 truncate mt-1">{url}</a>)}
            </div>
          </div>
        ))}
        {isLoading && <Loader2 className="animate-spin text-indigo-500" />}
        <div ref={messagesEndRef} />
      </div>
      <div className="p-4 bg-gray-900 border-t border-gray-800 flex gap-2">
        <button onClick={() => fileInputRef.current?.click()} className="p-3 bg-gray-800 rounded-xl text-gray-400"><Paperclip size={18}/></button>
        <div className="relative flex-1">
            <input value={input} onChange={(e) => setInput(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && handleSend()} className="w-full bg-gray-800 text-white rounded-xl px-4 py-3 outline-none" placeholder="Message..." />
            <button onClick={handleSend} disabled={isLoading} className="absolute right-2 top-2 p-1.5 bg-indigo-600 rounded-lg text-white"><Send size={18} /></button>
        </div>
      </div>
    </div>
  );
}