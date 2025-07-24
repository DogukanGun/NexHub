'use client';
import { useEffect, useRef, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Button } from './Button';

interface WelcomePopupProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  soundFile: string;
  message?: string;
}

export function WelcomePopup({ isOpen, onClose, title, soundFile, message }: WelcomePopupProps) {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const animationFrameRef = useRef<number>(0);

  useEffect(() => {
    if (!isOpen) return;

    let audioContext: AudioContext | undefined;
    let analyser: AnalyserNode | undefined;
    let audioSource: MediaElementAudioSourceNode | undefined;

    const setupAudio = async () => {
      if (!audioRef.current || !canvasRef.current) return;

      audioContext = new AudioContext();
      analyser = audioContext.createAnalyser();
      audioSource = audioContext.createMediaElementSource(audioRef.current);

      audioSource.connect(analyser);
      analyser.connect(audioContext.destination);

      analyser.fftSize = 256;
      const bufferLength = analyser.frequencyBinCount;
      const dataArray = new Uint8Array(bufferLength);

      const canvas = canvasRef.current;
      const ctx = canvas.getContext('2d');
      if (!ctx) return;

      const draw = () => {
        const WIDTH = canvas.width;
        const HEIGHT = canvas.height;

        animationFrameRef.current = requestAnimationFrame(draw);

        analyser?.getByteFrequencyData(dataArray);

        ctx.fillStyle = 'rgb(0, 0, 0)';
        ctx.fillRect(0, 0, WIDTH, HEIGHT);

        const barWidth = (WIDTH / bufferLength) * 2.5;
        let barHeight;
        let x = 0;

        for (let i = 0; i < bufferLength; i++) {
          barHeight = dataArray[i] / 2;

          const gradient = ctx.createLinearGradient(0, 0, 0, HEIGHT);
          gradient.addColorStop(0, '#F3BA2F');
          gradient.addColorStop(1, '#F3BA2F33');

          ctx.fillStyle = gradient;
          ctx.fillRect(x, HEIGHT - barHeight, barWidth, barHeight);

          x += barWidth + 1;
        }
      };

      audioRef.current.play();
      setIsPlaying(true);
      draw();
    };

    setupAudio();

    return () => {
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
      }
      if (audioContext) {
        audioContext.close();
      }
      setIsPlaying(false);
    };
  }, [isOpen]);

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80"
        >
          <motion.div
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.9, opacity: 0 }}
            className="relative bg-black/90 p-8 rounded-lg cyber-border max-w-lg w-full"
          >
            <div className="text-center space-y-6">
              <h2 className="text-4xl font-transformers gradient-text">{title}</h2>
              {message && (
                <p className="text-text-secondary font-mono">{message}</p>
              )}
              
              <div className="relative h-32 mb-8">
                <canvas
                  ref={canvasRef}
                  width={400}
                  height={128}
                  className="w-full h-full"
                />
                <audio
                  ref={audioRef}
                  src={soundFile}
                  onEnded={() => {
                    setIsPlaying(false);
                    onClose();
                  }}
                />
              </div>

              <Button
                variant="gradient"
                onClick={onClose}
                className="cyber-border w-full"
                disabled={isPlaying}
              >
                {isPlaying ? 'Processing...' : 'Continue'}
              </Button>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
} 