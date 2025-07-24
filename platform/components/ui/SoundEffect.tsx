'use client';
import { useEffect, useRef } from 'react';

interface SoundEffectProps {
  src: string;
  play: boolean;
  volume?: number;
}

export function SoundEffect({ src, play, volume = 0.5 }: SoundEffectProps) {
  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    if (play && audioRef.current) {
      audioRef.current.volume = volume;
      audioRef.current.currentTime = 0;
      audioRef.current.play();
    }
  }, [play, volume]);

  return <audio ref={audioRef} src={src} preload="auto" />;
} 