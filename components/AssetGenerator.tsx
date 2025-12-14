import React, { useState } from 'react';
import { AspectRatio, GeneratedImage } from '@/types';
import { generateAsset } from '../services/geminiService';
import { Image as ImageIcon, Loader2, Download } from 'lucide-react';

export const AssetGenerator: React.FC = () => {
  const [prompt, setPrompt] = useState('');
  const [ratio, setRatio] = useState<AspectRatio>("1:1");
  const [loading, setLoading] = useState(false);
  const [gallery, setGallery] = useState<GeneratedImage[]>([]);

  const handleGenerate = async () => {
    if (!prompt.trim()) return;
    setLoading(true);
    try {
        const url = await generateAsset(prompt, ratio);
        setGallery(prev => [{ url, prompt, ratio }, ...prev]);
        setPrompt('');
    } catch (e) {
        alert("Failed to generate image.");
    } finally {
        setLoading(false);
    }
  };

  const ratios: AspectRatio[] = ["1:1", "3:4", "4:3", "9:16", "16:9"];

  return (
    <div className="flex flex-col h-full bg-gray-900 p-6 overflow-y-auto">
        <h2 className="text-2xl font-bold text-white mb-6 flex items-center gap-2"><ImageIcon className="text-pink-500"/> Asset Generator</h2>
        <div className="bg-gray-800 rounded-xl p-6 border border-gray-700 mb-8">
            <textarea value={prompt} onChange={(e) => setPrompt(e.target.value)} placeholder="Prompt..." className="w-full bg-gray-900 border border-gray-700 rounded-lg p-3 text-white h-24 mb-4" />
            <div className="flex justify-between items-center">
                 <div className="flex gap-2">
                    {ratios.map(r => (
                        <button key={r} onClick={() => setRatio(r)} className={`px-2 py-1 text-xs rounded border ${ratio === r ? 'bg-pink-600 border-pink-500' : 'bg-gray-900 border-gray-700'}`}>{r}</button>
                    ))}
                 </div>
                 <button onClick={handleGenerate} disabled={loading} className="px-6 py-2 bg-pink-600 rounded-lg text-white flex items-center gap-2">
                    {loading ? <Loader2 className="animate-spin" size={18} /> : 'Generate'}
                 </button>
            </div>
        </div>
        <div className="grid grid-cols-2 lg:grid-cols-3 gap-6">
            {gallery.map((img, idx) => (
                <div key={idx} className="bg-gray-800 rounded-xl overflow-hidden border border-gray-700">
                    <img src={img.url} alt={img.prompt} className="w-full h-48 object-contain bg-black" />
                    <div className="p-2 text-xs text-gray-400">{img.prompt}</div>
                </div>
            ))}
        </div>
    </div>
  );
};