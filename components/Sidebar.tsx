import React from 'react';
import { FolderCode, MessageSquare, Image as ImageIcon, Download, Upload, Save } from 'lucide-react';
import { Tab } from '@/types';

interface SidebarProps {
  activeTab: Tab;
  setActiveTab: (tab: Tab) => void;
  onSave: () => void;
  onExportZip: () => void;
  onImport: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ activeTab, setActiveTab, onSave, onExportZip, onImport }) => {
  const btnClass = (tab: Tab) => `
    p-3 rounded-xl transition-all duration-200 flex flex-col items-center justify-center
    md:w-full md:mb-2
    ${activeTab === tab ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/30' : 'text-gray-400 hover:bg-gray-800 hover:text-gray-100'}
  `;

  return (
    <div className="
      fixed bottom-0 left-0 right-0 h-16 bg-gray-900 border-t border-gray-800 flex flex-row items-center justify-around z-50 px-2
      md:relative md:w-20 md:h-full md:flex-col md:border-r md:border-t-0 md:justify-start md:py-6
    ">
      <div className="hidden md:block mb-8">
        <div className="w-10 h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center shadow-lg">
           <FolderCode className="text-white w-6 h-6" />
        </div>
      </div>
      <nav className="flex flex-row w-full justify-around md:flex-col md:flex-1 md:w-full md:px-2 md:justify-start">
        <button onClick={() => setActiveTab(Tab.EDITOR)} className={btnClass(Tab.EDITOR)} title="Code Editor">
          <FolderCode size={24} />
          <span className="text-[10px] mt-1 hidden md:block">Code</span>
        </button>
        <button onClick={() => setActiveTab(Tab.CHAT)} className={btnClass(Tab.CHAT)} title="AI Chat">
          <MessageSquare size={24} />
          <span className="text-[10px] mt-1 hidden md:block">Chat</span>
        </button>
        <button onClick={() => setActiveTab(Tab.ASSETS)} className={btnClass(Tab.ASSETS)} title="Asset Generator">
          <ImageIcon size={24} />
          <span className="text-[10px] mt-1 hidden md:block">Assets</span>
        </button>
        
        <button onClick={onImport} className="md:hidden p-3 text-blue-400 hover:text-blue-300" title="Import Files">
             <Upload size={24} />
        </button>
        <button onClick={onExportZip} className="md:hidden p-3 text-emerald-400 hover:text-emerald-300" title="Export ZIP">
             <Download size={24} />
        </button>
      </nav>

      <div className="hidden md:flex w-full px-2 flex-col gap-2 mb-4">
        <div className="h-px bg-gray-800 w-full mb-2"></div>
        <button onClick={onSave} className="p-3 text-gray-400 hover:text-white hover:bg-gray-800 rounded-xl flex flex-col items-center" title="Save Project (JSON)">
          <Save size={20} />
          <span className="text-[10px] mt-1">Save</span>
        </button>
        <button onClick={onExportZip} className="p-3 text-emerald-500 hover:text-emerald-400 hover:bg-emerald-900/20 rounded-xl flex flex-col items-center" title="Export Source (ZIP)">
          <Download size={20} />
          <span className="text-[10px] mt-1">Export</span>
        </button>
         <button onClick={onImport} className="p-3 text-gray-400 hover:text-blue-400 hover:bg-gray-800 rounded-xl flex flex-col items-center" title="Import Project (JSON/ZIP)">
          <Upload size={20} />
          <span className="text-[10px] mt-1">Import</span>
        </button>
      </div>
    </div>
  );
};