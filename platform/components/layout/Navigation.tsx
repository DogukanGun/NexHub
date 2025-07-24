'use client';
import { useWallet } from '@/contexts/WalletContext';
import { Button } from '@/components/ui/Button';
import { motion } from 'framer-motion';
import Link from 'next/link';
import { Wallet } from 'lucide-react';
import Image from 'next/image';

export function Navigation() {
  const { address, isConnecting, connect, disconnect } = useWallet();

  return (
    <motion.nav
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      className="fixed top-0 left-0 right-0 z-50 bg-black/50 backdrop-blur-md border-b border-primary/20"
    >
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <Link 
          href="/" 
          className="flex items-center gap-3 group"
        >
          <div className="relative w-8 h-8 transform group-hover:scale-110 transition-transform">
            <Image
              src="/autobots_logo.svg"
              alt="Autobots Logo"
              fill
              className="object-contain"
              style={{ filter: 'invert(1) sepia(1) saturate(5) hue-rotate(359deg) brightness(0.95)' }}
            />
          </div>
          <span className="font-transformers text-2xl gradient-text relative">
            <span className="relative z-10">NexHub</span>
            <div className="absolute inset-0 bg-primary/20 opacity-0 group-hover:opacity-100 blur-xl transition-opacity" />
          </span>
        </Link>

        <div className="flex items-center gap-6">
          <Link
            href="/club"
            className="text-text-secondary hover:text-primary transition-colors relative group font-mono"
          >
            <span className="relative z-10">Enter Club</span>
            <div className="absolute bottom-0 left-0 w-0 h-0.5 bg-primary group-hover:w-full transition-all duration-300" />
          </Link>
          
          {address ? (
            <Button
              variant="outline"
              onClick={disconnect}
              className="cyber-border flex items-center gap-2 bg-black/60"
            >
              <div className="relative w-4 h-4">
                <Image
                  src="/autobots_logo.svg"
                  alt="Autobots Logo"
                  fill
                  className="object-contain"
                  style={{ filter: 'invert(1) sepia(1) saturate(5) hue-rotate(359deg) brightness(0.95)' }}
                />
              </div>
              <span className="font-mono text-sm">
                {address.slice(0, 6)}...{address.slice(-4)}
              </span>
            </Button>
          ) : (
            <Button
              variant="gradient"
              onClick={connect}
              disabled={isConnecting}
              className="cyber-border flex items-center gap-2"
            >
              <div className="relative w-4 h-4">
                <Image
                  src="/autobots_logo.svg"
                  alt="Autobots Logo"
                  fill
                  className="object-contain"
                  style={{ filter: 'invert(1) sepia(1) saturate(5) hue-rotate(359deg) brightness(0.95)' }}
                />
              </div>
              <span className="font-mono">
                {isConnecting ? 'Connecting...' : 'Connect Wallet'}
              </span>
            </Button>
          )}
        </div>
      </div>
    </motion.nav>
  );
} 