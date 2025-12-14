import { GoogleGenAI, Type } from "@google/genai";
import { VirtualFile, AspectRatio, AppStructureResponse, ProjectAnalysis, ChatAttachment } from "@/types";

// Initialize API
const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });

export const generateAppStructure = async (
  prompt: string,
  currentFiles: VirtualFile[]
): Promise<AppStructureResponse> => {
  const context = currentFiles.map(f => `File: ${f.name}\n\`\`\`${f.language}\n${f.content}\n\`\`\``).join('\n\n');
  
  const systemPrompt = `You are DevForge Architect.
  
  YOUR MISSION:
  Build complete, production-ready, and bug-free software solutions.
  
  CRITICAL RULES:
  1. **NO PLACEHOLDERS**: Write the FULL implementation.
  2. **INCREMENTAL BUILD**: Generate 1-2 high-quality files per turn.
  
  CONTEXT:
  Current Project Files:
  ${context}
  `;

  try {
    const response = await ai.models.generateContent({
      model: "gemini-3-pro-preview",
      contents: prompt,
      config: {
        systemInstruction: systemPrompt,
        thinkingConfig: { thinkingBudget: 32768 },
        responseMimeType: "application/json",
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            files: {
              type: Type.ARRAY,
              items: {
                type: Type.OBJECT,
                properties: {
                  name: { type: Type.STRING },
                  language: { type: Type.STRING },
                  content: { type: Type.STRING }
                },
                required: ["name", "language", "content"]
              }
            },
            status: { type: Type.STRING, enum: ["CONTINUE", "COMPLETE"] },
            reasoning: { type: Type.STRING }
          },
          required: ["files", "status", "reasoning"]
        }
      }
    });

    if (response.text) {
      return JSON.parse(response.text) as AppStructureResponse;
    }
    return { files: [], status: 'COMPLETE', reasoning: 'No response text generated.' };
  } catch (error) {
    console.error("App Generation Error:", error);
    throw error;
  }
};

export const analyzeProject = async (files: VirtualFile[]): Promise<ProjectAnalysis> => {
  const fileContext = files.map(f => `--- ${f.name} ---\n${f.content}`).join('\n\n');
  try {
    const response = await ai.models.generateContent({
      model: "gemini-3-pro-preview",
      contents: `Project Files:\n${fileContext}`,
      config: {
        systemInstruction: "You are DevForge Omni-Scanner. Analyze for bugs and fixes.",
        thinkingConfig: { thinkingBudget: 16000 },
        responseMimeType: "application/json",
        responseSchema: {
            type: Type.OBJECT,
            properties: {
                summary: { type: Type.STRING },
                criticalIssues: { type: Type.ARRAY, items: { type: Type.STRING } },
                fixes: {
                    type: Type.ARRAY,
                    items: {
                        type: Type.OBJECT,
                        properties: {
                            fileName: { type: Type.STRING },
                            description: { type: Type.STRING },
                            newContent: { type: Type.STRING }
                        },
                        required: ["fileName", "description", "newContent"]
                    }
                }
            },
            required: ["summary", "criticalIssues", "fixes"]
        }
      }
    });

    if (response.text) {
        return JSON.parse(response.text) as ProjectAnalysis;
    }
    throw new Error("Analysis failed");
  } catch (error) {
    return { summary: "Analysis Failed", criticalIssues: ["API Error"], fixes: [] };
  }
};

export const scanCodeForErrors = async (file: VirtualFile): Promise<string> => {
  try {
    const response = await ai.models.generateContent({
      model: "gemini-2.5-flash",
      contents: `Scan code for bugs:\n\nCode:\n${file.content}`,
    });
    return response.text || "No response.";
  } catch (error) {
    return "Error running scan.";
  }
};

export const chatWithBot = async (
  history: any[], 
  message: string,
  attachments: ChatAttachment[] = [],
  currentFiles?: VirtualFile[]
): Promise<string> => {
  try {
    let systemInstruction = "You are DevForge AI.";
    if (currentFiles && currentFiles.length > 0) {
      const filesContext = currentFiles.map(f => 
        `File: ${f.name} (${f.language})\n\`\`\`${f.language}\n${f.content}\n\`\`\``
      ).join('\n\n');
      systemInstruction += `\n\n=== PROJECT FILES ===\n${filesContext}`;
    }

    const chat = ai.chats.create({
      model: "gemini-3-pro-preview",
      history: history,
      config: { systemInstruction }
    });
    
    const parts: any[] = [];
    if (message.trim()) parts.push({ text: message });
    attachments.forEach(att => {
        const base64Data = att.data.includes(',') ? att.data.split(',')[1] : att.data;
        parts.push({ inlineData: { mimeType: att.type, data: base64Data } });
    });

    const result = await chat.sendMessage({ message: parts });
    return result.text || "";
  } catch (error) {
    throw error;
  }
};

export const searchWeb = async (query: string): Promise<{ text: string, sources: string[] }> => {
  try {
    const response = await ai.models.generateContent({
      model: "gemini-2.5-flash",
      contents: query,
      config: { tools: [{ googleSearch: {} }] }
    });
    const sources: string[] = [];
    response.candidates?.[0]?.groundingMetadata?.groundingChunks?.forEach((chunk: any) => {
        if (chunk.web?.uri) sources.push(chunk.web.uri);
    });
    return { text: response.text || "No results.", sources };
  } catch (error) {
    throw error;
  }
};

export const generateAsset = async (prompt: string, aspectRatio: AspectRatio): Promise<string> => {
  try {
    const response = await ai.models.generateContent({
      model: "gemini-2.5-flash-image",
      contents: { parts: [{ text: prompt }] },
      config: { imageConfig: { aspectRatio: aspectRatio } }
    });
    for (const part of response.candidates?.[0]?.content?.parts || []) {
      if (part.inlineData) return `data:image/png;base64,${part.inlineData.data}`;
    }
    throw new Error("No image data");
  } catch (error) {
    throw error;
  }
};