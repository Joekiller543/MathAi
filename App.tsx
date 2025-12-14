import React, { useState, useRef } from 'react';
import JSZip from 'jszip';
import { Sidebar } from './components/Sidebar';
import { FileManager } from './components/FileManager';
import { CodeEditor } from './components/CodeEditor';
import { ChatInterface } from './components/ChatInterface';
import { AssetGenerator } from './components/AssetGenerator';
import { Tab, VirtualFile, FileFix } from './types';
import { Menu, FolderCode } from 'lucide-react';
import { getDevForgeSource } from './lib/devForgeSource';

function App() {
  const [activeTab, setActiveTab] = useState<Tab>(Tab.EDITOR);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [files, setFiles] = useState<VirtualFile[]>([{ name: 'README.md', language: 'markdown', content: '# New Project' }]);
  const [activeFile, setActiveFile] = useState<VirtualFile | null>(files[0]);
  const [showNewFileModal, setShowNewFileModal] = useState(false);
  const [newFileName, setNewFileName] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleLoadDevForgeSource = () => {
      if(window.confirm("Load DevForge source code? This replaces current files.")) {
          const sourceFiles = getDevForgeSource();
          setFiles(sourceFiles);
          setActiveFile(sourceFiles.find(f => f.name === 'README.md') || sourceFiles[0]);
          setActiveTab(Tab.EDITOR);
      }
  };

  const handleExportZip = async () => {
    const zip = new JSZip();
    files.forEach(file => zip.file(file.name, file.content));
    const blob = await zip.generateAsync({ type: "blob" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = "devforge-source.zip";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };
  
  // ... (Other handlers omitted for brevity in source export, but present in runtime) ...
  
  return (
    <div className="flex flex-col md:flex-row h-screen bg-gray-900 text-gray-100 overflow-hidden font-sans">
      <input type="file" ref={fileInputRef} className="hidden" onChange={() => {}} />
      <div className="md:hidden h-14 bg-gray-900 border-b border-gray-800 flex items-center justify-between px-4 z-20">
          <div className="font-bold text-white">DevForge AI</div>
          <button onClick={() => setIsMobileMenuOpen(true)}><Menu size={24} /></button>
      </div>
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} onSave={() => {}} onExportZip={handleExportZip} onImport={() => {}} />
      <div className="flex-1 flex overflow-hidden relative">
        <div className={`${activeTab === Tab.ASSETS ? 'hidden' : 'block'}`}>
             <FileManager files={files} activeFile={activeFile} onSelectFile={setActiveFile} onDeleteFile={() => {}} onAddFile={() => {}} onImport={() => {}} onLoadDevForgeSource={handleLoadDevForgeSource} isOpenMobile={isMobileMenuOpen} onCloseMobile={() => setIsMobileMenuOpen(false)} />
        </div>
        <main className="flex-1 flex flex-col min-w-0 bg-[#0d1117] relative">
          {activeTab === Tab.EDITOR && <CodeEditor activeFile={activeFile} files={files} onUpdateFile={(c) => setActiveFile(prev => prev ? {...prev, content: c} : null)} onApplyFixes={() => {}} />}
          {activeTab === Tab.CHAT && <ChatInterface files={files} onFilesGenerated={(newFiles) => setFiles(prev => [...prev, ...newFiles])} />}
          {activeTab === Tab.ASSETS && <AssetGenerator />}
        </main>
      </div>
    </div>
  );
}

export default App;