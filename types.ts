export interface VirtualFile {
  name: string;
  language: string;
  content: string;
}

export interface ChatAttachment {
  name: string;
  type: string;
  data: string; // base64
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'model';
  text: string;
  timestamp: number;
  isError?: boolean;
  groundingMetadata?: string[];
  attachments?: ChatAttachment[];
}

export type AspectRatio = "1:1" | "2:3" | "3:2" | "3:4" | "4:3" | "9:16" | "16:9" | "21:9";

export interface GeneratedImage {
  url: string;
  prompt: string;
  ratio: AspectRatio;
}

export interface AppStructureResponse {
  files: VirtualFile[];
  status: 'CONTINUE' | 'COMPLETE';
  reasoning: string;
}

export interface FileFix {
  fileName: string;
  description: string;
  newContent: string;
}

export interface ProjectAnalysis {
  summary: string;
  criticalIssues: string[];
  fixes: FileFix[];
}

export enum Tab {
  EDITOR = 'editor',
  CHAT = 'chat',
  SETTINGS = 'settings',
  ASSETS = 'assets'
}