'use client';
import { Navigation } from '@/components/layout/Navigation';
import { Button } from '@/components/ui/Button';
import { motion } from 'framer-motion';
import Link from 'next/link';
import { Shield, Zap, Lock, BrainCircuit } from 'lucide-react';
import Image from 'next/image';

export default function Home() {
  return (
    <div className="hex-pattern min-h-screen">
      <Navigation />
      <main className="relative mt-20">
        {/* Hero Section */}
        <section className="min-h-screen flex items-center justify-center relative overflow-hidden">
          <div className="absolute inset-0 z-0">
            <div className="absolute inset-0 bg-gradient-to-b from-black via-transparent to-black opacity-80" />
            {/* Animated hexagon grid background would be here */}
          </div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="container mx-auto px-4 text-center relative z-10"
          >
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.2, duration: 1 }}
              className="mb-8"
            >
              <div className="w-40 h-40 mx-auto mb-6 relative">
                <div className="absolute inset-0 bg-primary opacity-20 rounded-full glowing" />
                <div className="absolute inset-2 bg-primary opacity-10 rounded-full pulsing" />
                <div className="absolute inset-4 border-2 border-primary rounded-full rotating" />
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="relative w-24 h-24 transform hover:scale-110 transition-transform">
                    <Image
                      src="/autobots_logo.svg"
                      alt="Autobots Logo"
                      fill
                      className="object-contain"
                      style={{ filter: 'invert(1) sepia(1) saturate(5) hue-rotate(359deg) brightness(0.95)' }}
                    />
                  </div>
                </div>
              </div>
              <h1 className="text-6xl md:text-8xl font-bold mb-6 gradient-text">
                NexHub
              </h1>
            </motion.div>

            <motion.p
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4 }}
              className="text-xl md:text-2xl text-text-secondary mb-12 max-w-3xl mx-auto"
            >
              Welcome to the future of token launches. Only the worthy shall enter.
            </motion.p>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.6 }}
              className="flex flex-col sm:flex-row gap-6 justify-center mb-20"
            >
              <Link href="/club">
                <Button
                  variant="gradient"
                  size="lg"
                  className="cyber-border glowing px-12 py-6 text-lg group"
                >
                  <span className="relative flex items-center gap-3">
                    Enter the Hub
                    <div className="relative w-6 h-6 transform group-hover:scale-110 transition-transform">
                      <Image
                        src="/autobots_logo.svg"
                        alt="Autobots Logo"
                        fill
                        className="object-contain"
                        style={{ filter: 'invert(1) sepia(1) saturate(5) hue-rotate(359deg) brightness(0.95)' }}
                      />
                    </div>
                  </span>
                </Button>
              </Link>
            </motion.div>

            {/* Feature Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 mt-32">
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.8 }}
                className="transformer-card p-8 rounded-lg"
              >
                <div className="w-16 h-16 mx-auto mb-6 bg-primary/10 rounded-lg flex items-center justify-center">
                  <div className="relative w-8 h-8">
                    <Image
                      src="/autobots_logo.svg"
                      alt="Autobots Logo"
                      fill
                      className="object-contain"
                      style={{ filter: 'invert(1) sepia(1) saturate(5) hue-rotate(359deg) brightness(0.95)' }}
                    />
                  </div>
                </div>
                <h3 className="text-xl font-bold mb-4 text-primary">Elite Access</h3>
                <p className="text-text-secondary">
                  Only those with true knowledge may enter. Prove your worth.
                </p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 1 }}
                className="transformer-card p-8 rounded-lg"
              >
                <div className="w-16 h-16 mx-auto mb-6 bg-primary/10 rounded-lg flex items-center justify-center">
                  <BrainCircuit className="w-8 h-8 text-primary" />
                </div>
                <h3 className="text-xl font-bold mb-4 text-primary">AI Verification</h3>
                <p className="text-text-secondary">
                  Advanced AI ensures only true believers gain access.
                </p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 1.2 }}
                className="transformer-card p-8 rounded-lg"
              >
                <div className="w-16 h-16 mx-auto mb-6 bg-primary/10 rounded-lg flex items-center justify-center">
                  <Lock className="w-8 h-8 text-primary" />
                </div>
                <h3 className="text-xl font-bold mb-4 text-primary">Secure Transactions</h3>
                <p className="text-text-secondary">
                  Circle-powered security for seamless token purchases.
                </p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 1.4 }}
                className="transformer-card p-8 rounded-lg"
              >
                <div className="w-16 h-16 mx-auto mb-6 bg-primary/10 rounded-lg flex items-center justify-center">
                  <Zap className="w-8 h-8 text-primary" />
                </div>
                <h3 className="text-xl font-bold mb-4 text-primary">Instant Access</h3>
                <p className="text-text-secondary">
                  Pass the test, gain immediate access to token launches.
                </p>
              </motion.div>
            </div>
          </motion.div>
        </section>

        {/* Additional Sections */}
        <section className="py-32 relative">
          <div className="container mx-auto px-4">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8 }}
              className="text-center mb-16"
            >
              <h2 className="text-4xl md:text-5xl font-bold mb-6 gradient-text">
                The Future of Token Launches
              </h2>
              <p className="text-xl text-text-secondary max-w-3xl mx-auto">
                Join an elite community of verified investors who understand and believe in the projects they support.
              </p>
            </motion.div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-16 items-center">
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.4 }}
                className="space-y-8"
              >
                <div className="transformer-card p-8 rounded-lg">
                  <h3 className="text-2xl font-bold mb-4 text-primary">Knowledge is Power</h3>
                  <p className="text-text-secondary">
                    Our AI-powered verification system ensures that only those who truly understand the project can participate.
                  </p>
                </div>
                <div className="transformer-card p-8 rounded-lg">
                  <h3 className="text-2xl font-bold mb-4 text-primary">Secure by Design</h3>
                  <p className="text-text-secondary">
                    Built on Circle's infrastructure, ensuring the highest level of security for your investments.
                  </p>
                </div>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.6 }}
                className="relative"
              >
                <div className="aspect-square rounded-lg overflow-hidden cyber-border">
                  <div className="absolute inset-0 bg-gradient-to-br from-primary/20 to-transparent" />
                  <div className="absolute inset-0 flex items-center justify-center">
                    <div className="relative w-40 h-40 transform hover:scale-110 transition-transform">
                      <Image
                        src="/autobots_logo.svg"
                        alt="Autobots Logo"
                        fill
                        className="object-contain"
                        style={{ filter: 'invert(1) sepia(1) saturate(5) hue-rotate(359deg) brightness(0.95)' }}
                      />
                    </div>
                  </div>
                </div>
              </motion.div>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
