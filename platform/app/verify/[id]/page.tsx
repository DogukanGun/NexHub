'use client';
import { Navigation } from '@/components/layout/Navigation';
import { Button } from '@/components/ui/Button';
import { SoundEffect } from '@/components/ui/SoundEffect';
import { WelcomePopup } from '@/components/ui/WelcomePopup';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, Bot, Loader, Shield, Zap, Brain } from 'lucide-react';
import Image from 'next/image';
import { useEffect, useState, useRef } from 'react';

interface Message {
  role: 'user' | 'assistant';
  content: string;
  id: string;
}

interface VerifyPageProps {
  params: {
    id: string;
  };
}

export default function VerifyPage({ params }: VerifyPageProps) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [playSound, setPlaySound] = useState(false);
  const [playTyping, setPlayTyping] = useState(false);
  const [scanningPhase, setScanningPhase] = useState(true);
  const [showWelcomePopup, setShowWelcomePopup] = useState(true);
  const [showAcceptedPopup, setShowAcceptedPopup] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);

  // Scroll to bottom when messages update
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Initialize conversation with AI after scanning phase
  useEffect(() => {
    if (!scanningPhase && !showWelcomePopup) {
      const initialMessage: Message = {
        role: 'assistant',
        content: 'INITIATING KNOWLEDGE VERIFICATION PROTOCOL...\n\nGreetings, potential ally. I am Sentinel Prime, guardian of the NexHub knowledge vault. Before you can access our sacred tokens, you must prove your understanding. Are you prepared to begin?',
        id: 'initial',
      };
      setMessages([initialMessage]);
      setPlaySound(true);
    }
  }, [scanningPhase, showWelcomePopup]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMessage: Message = {
      role: 'user',
      content: input,
      id: `user-${Date.now()}`,
    };

    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);
    setPlaySound(true);

    try {
      // Simulate AI thinking with typing sound
      setPlayTyping(true);
      await new Promise(resolve => setTimeout(resolve, 2000));
      setPlayTyping(false);
      
      // For demo purposes, show accepted popup after first message
      if (messages.length === 1) {
        setShowAcceptedPopup(true);
        return;
      }

      const aiResponse: Message = {
        role: 'assistant',
        content: 'Your knowledge circuits seem to be functioning well. Let us delve deeper into the protocol specifications. How does the token distribution mechanism maintain equilibrium?',
        id: `ai-${Date.now()}`,
      };

      setMessages(prev => [...prev, aiResponse]);
      setPlaySound(true);
    } catch (error) {
      console.error('Failed to get AI response:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="hex-pattern min-h-screen relative overflow-hidden">
      <Navigation />
      
      {/* Sound Effects */}
      <SoundEffect src="/sounds/scan.mp3" play={scanningPhase} />
      <SoundEffect src="/sounds/message.mp3" play={playSound} volume={0.3} />
      <SoundEffect src="/sounds/typing.mp3" play={playTyping} volume={0.1} />

      {/* Welcome Popup */}
      <WelcomePopup
        isOpen={showWelcomePopup}
        onClose={() => {
          setShowWelcomePopup(false);
          setScanningPhase(false);
        }}
        title="Welcome to the Hub"
        message="Prepare for knowledge verification protocol"
        soundFile="/sounds/optimus_hi.mov"
      />

      {/* Accepted Popup */}
      <WelcomePopup
        isOpen={showAcceptedPopup}
        onClose={() => {
          setShowAcceptedPopup(false);
          window.location.href = `/buy/${params.id}`;
        }}
        title="Access Granted"
        message="Your knowledge has been verified. Welcome to NexHub."
        soundFile="/sounds/accepted_user.mov"
      />

      {/* Scanning Phase Overlay */}
      <AnimatePresence>
        {scanningPhase && (
          <motion.div
            initial={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-20 flex items-center justify-center bg-black"
          >
            <div className="relative">
              <motion.div
                animate={{
                  scale: [1, 1.2, 1],
                  rotate: [0, 180, 360],
                }}
                transition={{
                  duration: 3,
                  ease: "linear",
                  repeat: 0,
                }}
                className="w-32 h-32 border-4 border-primary rounded-full flex items-center justify-center"
              >
                <div className="relative w-16 h-16">
                  <Image
                    src="/autobots_logo.svg"
                    alt="Autobots Logo"
                    fill
                    className="object-contain"
                    style={{ filter: 'invert(1) sepia(1) saturate(5) hue-rotate(359deg) brightness(0.95)' }}
                  />
                </div>
              </motion.div>
              <motion.div
                animate={{
                  opacity: [0, 1, 0],
                }}
                transition={{
                  duration: 1,
                  repeat: 2,
                }}
                className="absolute -bottom-12 left-1/2 transform -translate-x-1/2 text-primary font-mono"
              >
                SCANNING...
              </motion.div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <main className="pt-24 pb-12 px-4">
        <div className="container mx-auto max-w-4xl">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-8"
          >
            <div className="flex items-center justify-center gap-4 mb-6">
              <div className="relative w-12 h-12">
                <Image
                  src="/autobots_logo.svg"
                  alt="Autobots Logo"
                  fill
                  className="object-contain"
                  style={{ filter: 'invert(1) sepia(1) saturate(5) hue-rotate(359deg) brightness(0.95)' }}
                />
              </div>
              <h1 className="text-4xl md:text-6xl font-bold gradient-text font-transformers">
                Knowledge Verification
              </h1>
            </div>
            <div className="flex items-center justify-center gap-2 text-xl text-text-secondary">
              <Brain className="w-6 h-6 text-primary" />
              <span>Prove your worth to access the vault</span>
            </div>
          </motion.div>

          <div className="cyber-border bg-black/40 backdrop-blur-sm rounded-xl overflow-hidden">
            <div className="h-[600px] overflow-y-auto p-6 space-y-4 relative">
              {/* Decorative Elements */}
              <div className="absolute top-0 left-0 w-full h-32 bg-gradient-to-b from-primary/10 to-transparent pointer-events-none" />
              <div className="absolute bottom-0 left-0 w-full h-32 bg-gradient-to-t from-primary/10 to-transparent pointer-events-none" />
              
              {messages.map((message) => (
                <motion.div
                  key={message.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className={`flex items-start gap-3 ${
                    message.role === 'assistant' ? 'flex-row' : 'flex-row-reverse'
                  }`}
                >
                  {message.role === 'assistant' ? (
                    <div className="w-12 h-12 rounded-lg cyber-border bg-primary/20 flex items-center justify-center relative overflow-hidden">
                      <div className="absolute inset-0 bg-gradient-to-br from-primary/30 to-transparent" />
                      <div className="relative w-6 h-6">
                        <Image
                          src="/autobots_logo.svg"
                          alt="Autobots Logo"
                          fill
                          className="object-contain"
                          style={{ filter: 'invert(1) sepia(1) saturate(5) hue-rotate(359deg) brightness(0.95)' }}
                        />
                      </div>
                    </div>
                  ) : (
                    <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center">
                      <Shield className="w-6 h-6 text-primary" />
                    </div>
                  )}
                  <div
                    className={`p-4 rounded-lg max-w-[80%] relative ${
                      message.role === 'assistant'
                        ? 'cyber-border bg-black/60'
                        : 'bg-primary text-black font-medium'
                    }`}
                  >
                    <div className="relative z-10">{message.content}</div>
                    {message.role === 'assistant' && (
                      <div className="absolute inset-0 bg-gradient-to-r from-primary/5 to-transparent" />
                    )}
                  </div>
                </motion.div>
              ))}
              
              {isLoading && (
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  className="flex items-center gap-2 text-primary"
                >
                  <Loader className="w-4 h-4 animate-spin" />
                  <span className="font-mono">PROCESSING_RESPONSE...</span>
                </motion.div>
              )}
              <div ref={chatEndRef} />
            </div>

            <div className="relative">
              <div className="absolute left-0 right-0 h-px bg-gradient-to-r from-transparent via-primary to-transparent" />
              <form onSubmit={handleSubmit} className="p-4">
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    placeholder="Enter your response..."
                    className="flex-1 bg-black/60 text-text-primary rounded-lg border border-primary/20 px-4 py-3 focus:outline-none focus:border-primary transition-colors placeholder:text-text-secondary font-mono"
                  />
                  <Button
                    type="submit"
                    variant="gradient"
                    disabled={!input.trim() || isLoading}
                    className="cyber-border relative group"
                  >
                    <div className="absolute inset-0 bg-gradient-to-r from-primary/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
                    <Send className="w-5 h-5 relative z-10" />
                  </Button>
                </div>
              </form>
            </div>
          </div>

          {/* Decorative Corner Elements */}
          <div className="fixed top-4 right-4 w-32 h-32 border border-primary/20 rounded-full animate-spin-slow" />
          <div className="fixed bottom-4 left-4 w-24 h-24 border border-primary/20 rounded-full animate-spin-slow" />
        </div>
      </main>
    </div>
  );
} 