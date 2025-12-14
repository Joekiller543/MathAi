import React from 'react';
import { VirtualFile } from '@/types';
import { FileCode, FileJson, FileType, Plus, Trash2, X, Upload, DownloadCloud } from 'lucide-react';

interface FileManagerProps {
  files: VirtualFile[];
  activeFile: VirtualFile | null;
  onSelectFile: (file: VirtualFile) => void;
  onDeleteFile: (fileName: string) => void;
  onAddFile: () => void;
  onImport: () => void;
  onLoadDevForgeSource: () => void;
  isOpenMobile: boolean;
  onCloseMobile: () => void;
}

export const FileManager: React.FC<FileManagerProps> = ({ 
  files, 
  activeFile, 
  onSelectFile, 
  onDeleteFile,
  onAddFile,
  onImport,
  onLoadDevForgeSource,
  isOpenMobile,
  onCloseMobile
}) => {
  const getIcon = (name: string) => {
    if (name.endsWith('.json')) return <FileJson size={16} className="text-yellow-400" />;
    if (name.endsWith('.tsx') || name.endsWith('.ts')) return <FileCode size={16} className="text-blue-400" />;
    if (name.endsWith('.js') || name.endsWith('.jsx')) return <FileCode size={16} className="text-yellow-300" />;
    if (name.endsWith('.css')) return <FileType size={16} className="text-pink-400" />;
    return <FileType size={16} className="text-gray-400" />;
  };

  const containerClasses = `
    flex-col h-full bg-gray-800 border-r border-gray-700
    md:flex md:w-64 md:static
    ${isOpenMobile ? 'fixed inset-0 z-40 flex w-full' : 'hidden'}
  `;

  return (
    <div className={containerClasses}>
      <div className="p-4 border-b border-gray-700 flex justify-between items-center bg-gray-900">
        <h2 className="text-sm font-semibold text-gray-300 tracking-wider">PROJECT FILES</h2>
        <div className="flex gap-1">
            <button onClick={onLoadDevForgeSource} className="p-1 hover:bg-gray-700 rounded text-gray-400 hover:text-purple-400 transition-colors" title="Load DevForge Source Code">
                <DownloadCloud size={16} />
            </button>
            <button onClick={onImport} className="p-1 hover:bg-gray-700 rounded text-gray-400 hover:text-blue-400 transition-colors" title="Import Files">
                <Upload size={16} />
            </button>
            <button onClick={onAddFile} className="p-1 hover:bg-gray-700 rounded text-gray-400 hover:text-white transition-colors" title="New File">
                <Plus size={16} />
            </button>
            <button onClick={onCloseMobile} className="md:hidden p-1 hover:bg-gray-700 rounded text-gray-400 hover:text-white transition-colors">
                <X size={16} />
            </button>
        </div>
      </div>
      <div className="flex-1 overflow-y-auto p-2 space-y-1">
        {files.map((file) => (
          <div
            key={file.name}
            className={`
              group flex items-center justify-between px-3 py-3 md:py-2 rounded-md cursor-pointer text-sm transition-all
              ${activeFile?.name === file.name 
                ? 'bg-indigo-600/20 text-indigo-300 border border-indigo-500/30' 
                : 'text-gray-400 hover:bg-gray-700/50 hover:text-gray-200 border border-transparent'}
            `}
            onClick={() => {
                onSelectFile(file);
                onCloseMobile();
            }}
          >
            <div className="flex items-center gap-3 overflow-hidden">
              {getIcon(file.name)}
              <span className="truncate">{file.name}</span>
            </div>
            <button 
                onClick={(e) => { e.stopPropagation(); onDeleteFile(file.name); }}
                className="md:opacity-0 group-hover:opacity-100 p-1 hover:text-red-400 transition-opacity"
            >
                <Trash2 size={14} />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};