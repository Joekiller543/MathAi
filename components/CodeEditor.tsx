import React, { useState } from 'react';
import { VirtualFile, ProjectAnalysis, FileFix } from '@/types';
import { Play, Bug, ShieldCheck, CheckCircle, AlertTriangle, X, Zap } from 'lucide-react';
import { scanCodeForErrors, analyzeProject } from '../services/geminiService';

interface CodeEditorProps {
  activeFile: VirtualFile | null;
  files: VirtualFile[];
  onUpdateFile: (newContent: string) => void;
  onApplyFixes: (fixes: FileFix[]) => void;
}

export const CodeEditor: React.FC<CodeEditorProps> = ({ activeFile, files, onUpdateFile, onApplyFixes }) => {
  const [isScanning, setIsScanning] = useState(false);
  const [scanResult, setScanResult] = useState<string | null>(null);
  const [isAuditing, setIsAuditing] = useState(false);
  const [auditResult, setAuditResult] = useState<ProjectAnalysis | null>(null);

  if (!activeFile) return <div className="flex-1 bg-gray-900 text-gray-500 flex items-center justify-center">Select a file.</div>;

  const handleQuickScan = async () => {
    setIsScanning(true);
    setScanResult(null);
    const result = await scanCodeForErrors(activeFile);
    setScanResult(result);
    setIsScanning(false);
  };

  const handleDeepAudit = async () => {
    setIsAuditing(true);
    setAuditResult(null);
    setScanResult(null);
    const result = await analyzeProject(files);
    setAuditResult(result);
    setIsAuditing(false);
  };

  const handleApplyFixes = () => {
      if (auditResult?.fixes) {
          onApplyFixes(auditResult.fixes);
          setAuditResult(null);
          alert(`Applied ${auditResult.fixes.length} fixes.`);
      }
  };

  return (
    <div className="flex-1 flex flex-col h-full bg-[#0d1117] overflow-hidden relative">
      <div className="h-12 bg-gray-800 border-b border-gray-700 flex items-center px-4 justify-between">
        <div className="flex items-center gap-2">
            <span className="text-gray-400 text-xs uppercase font-semibold">{activeFile.language}</span>
            <span className="text-gray-600">/</span>
            <span className="text-gray-200 font-medium text-sm">{activeFile.name}</span>
        </div>
        <div className="flex gap-2">
            <button onClick={handleQuickScan} disabled={isScanning} className="flex items-center gap-2 px-3 py-1.5 bg-gray-700 hover:bg-gray-600 text-gray-300 rounded text-xs">
                <Bug size={14} /> {isScanning ? 'Scanning...' : 'Quick Scan'}
            </button>
            <button onClick={handleDeepAudit} disabled={isAuditing} className="flex items-center gap-2 px-3 py-1.5 bg-indigo-900/40 text-indigo-400 border border-indigo-500/30 rounded text-xs">
                <ShieldCheck size={14} /> {isAuditing ? 'Auditing...' : 'Deep Audit'}
            </button>
        </div>
      </div>
      <div className="flex-1 relative flex">
         <div className="absolute inset-0 flex">
            <div className="w-12 bg-[#0d1117] border-r border-gray-800 text-right pr-3 pt-4 text-gray-600 font-mono text-sm select-none">
                {activeFile.content.split('\n').map((_, i) => <div key={i} className="leading-6">{i + 1}</div>)}
            </div>
            <textarea
                className="flex-1 bg-[#0d1117] text-gray-300 font-mono text-sm p-4 leading-6 outline-none resize-none"
                value={activeFile.content}
                onChange={(e) => onUpdateFile(e.target.value)}
                spellCheck={false}
            />
         </div>
      </div>
      {scanResult && (
          <div className="absolute bottom-0 left-0 right-0 max-h-64 bg-gray-900 border-t border-gray-700 shadow-2xl flex flex-col z-20">
              <div className="flex justify-between items-center p-2 bg-gray-800 px-4">
                  <h3 className="text-xs font-bold text-gray-300 uppercase flex items-center gap-2">Result</h3>
                  <button onClick={() => setScanResult(null)} className="text-gray-400 text-xs">Close</button>
              </div>
              <div className="p-4 overflow-y-auto text-sm text-gray-300 font-mono whitespace-pre-wrap">{scanResult}</div>
          </div>
      )}
      {auditResult && (
        <div className="absolute inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-6">
            <div className="bg-gray-800 w-full max-w-2xl rounded-xl border border-gray-700 shadow-2xl flex flex-col max-h-[90vh]">
                <div className="p-4 border-b border-gray-700 flex justify-between items-center bg-gray-900 rounded-t-xl">
                     <h3 className="text-lg font-bold text-white">Project Audit Report</h3>
                     <button onClick={() => setAuditResult(null)} className="p-1 hover:bg-gray-800 rounded"><X size={20}/></button>
                </div>
                <div className="p-6 overflow-y-auto flex-1 space-y-6">
                    <div className="bg-gray-900/50 p-4 rounded-lg border border-gray-700">
                        <h4 className="text-sm font-semibold text-gray-300 mb-2">Summary</h4>
                        <p className="text-gray-400 text-sm">{auditResult.summary}</p>
                    </div>
                    {auditResult.fixes.length > 0 && (
                        <div>
                             <h4 className="text-sm font-semibold text-indigo-400 mb-2">Suggested Fixes</h4>
                             {auditResult.fixes.map((fix, idx) => (
                                <div key={idx} className="bg-gray-900 p-3 rounded border border-gray-700 mb-2">
                                    <span className="text-xs font-mono text-indigo-300 bg-indigo-900/30 px-2 py-0.5 rounded">{fix.fileName}</span>
                                    <p className="text-xs text-gray-400 mt-2">{fix.description}</p>
                                </div>
                             ))}
                        </div>
                    )}
                </div>
                <div className="p-4 border-t border-gray-700 bg-gray-900 rounded-b-xl flex justify-end gap-3">
                    <button onClick={() => setAuditResult(null)} className="px-4 py-2 text-gray-400 text-sm">Dismiss</button>
                    {auditResult.fixes.length > 0 && (
                        <button onClick={handleApplyFixes} className="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg flex items-center gap-2">
                            <Zap size={16} /> Auto-Fix All
                        </button>
                    )}
                </div>
            </div>
        </div>
      )}
    </div>
  );
};