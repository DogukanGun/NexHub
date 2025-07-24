'use client';
import { Navigation } from '@/components/layout/Navigation';
import { Button } from '@/components/ui/Button';
import { motion } from 'framer-motion';
import Link from 'next/link';
import { ArrowRight, Shield, Users, Clock, Sparkles } from 'lucide-react';

interface Launchpad {
  id: string;
  name: string;
  description: string;
  totalRaise: string;
  participants: number;
  status: 'active' | 'upcoming' | 'ended';
  image: string;
}

// This would typically come from an API
const launchpads: Launchpad[] = [
  {
    id: 'nexhub-token',
    name: 'NexHub Token',
    description: 'The native token of the NexHub ecosystem, powering the next generation of token launches.',
    totalRaise: '1,000,000 USDC',
    participants: 500,
    status: 'active',
    image: '/nexhub-logo.png', // You'll need to add this image
  },
  {
    id: 'defi-protocol',
    name: 'DeFi Protocol',
    description: 'Revolutionary DeFi protocol bringing institutional liquidity to DeFi.',
    totalRaise: '2,000,000 USDC',
    participants: 750,
    status: 'upcoming',
    image: '/defi-logo.png', // You'll need to add this image
  },
];

export default function ClubPage() {
  return (
    <div className="hex-pattern min-h-screen">
      <Navigation />
      <main className="pt-24 pb-12 px-4">
        <div className="container mx-auto max-w-6xl">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-16"
          >
            <h1 className="text-5xl md:text-6xl font-bold mb-6 gradient-text">
              The Hub
            </h1>
            <p className="text-xl text-text-secondary max-w-2xl mx-auto">
              Select your mission. Prove your knowledge. Join the elite.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 gap-8">
            {launchpads.map((launchpad, index) => (
              <motion.div
                key={launchpad.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.2 }}
                className="transformer-card relative overflow-hidden rounded-xl p-1"
              >
                <div className="absolute inset-0 bg-gradient-to-r from-primary/20 via-primary/10 to-transparent" />
                
                <div className="relative bg-background/80 backdrop-blur-sm rounded-lg p-8">
                  <div className="grid md:grid-cols-2 gap-8 items-center">
                    <div className="space-y-6">
                      <div className="flex items-center justify-between">
                        <h2 className="text-3xl font-bold text-primary">{launchpad.name}</h2>
                        <span className={`
                          px-4 py-2 rounded-full text-sm font-medium cyber-border
                          ${launchpad.status === 'active' ? 'text-green-400 border-green-400' : ''}
                          ${launchpad.status === 'upcoming' ? 'text-primary border-primary' : ''}
                          ${launchpad.status === 'ended' ? 'text-neutral-400 border-neutral-400' : ''}
                        `}>
                          {launchpad.status.charAt(0).toUpperCase() + launchpad.status.slice(1)}
                        </span>
                      </div>

                      <p className="text-text-secondary text-lg leading-relaxed">
                        {launchpad.description}
                      </p>

                      <div className="grid grid-cols-2 gap-4">
                        <div className="transformer-card p-4 rounded-lg">
                          <div className="flex items-center gap-2 mb-2">
                            <Shield className="w-5 h-5 text-primary" />
                            <span className="text-text-secondary">Total Raise</span>
                          </div>
                          <span className="text-lg font-bold text-primary">
                            {launchpad.totalRaise}
                          </span>
                        </div>

                        <div className="transformer-card p-4 rounded-lg">
                          <div className="flex items-center gap-2 mb-2">
                            <Users className="w-5 h-5 text-primary" />
                            <span className="text-text-secondary">Participants</span>
                          </div>
                          <span className="text-lg font-bold text-primary">
                            {launchpad.participants}
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="relative aspect-square rounded-lg overflow-hidden cyber-border">
                      <div className="absolute inset-0 flex items-center justify-center">
                        <div className="w-32 h-32 border-4 border-primary rounded-full glowing flex items-center justify-center">
                          <Sparkles className="w-12 h-12 text-primary" />
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="mt-8 flex justify-end">
                    <Link href={`/verify/${launchpad.id}`}>
                      <Button
                        variant="gradient"
                        size="lg"
                        className="cyber-border group/button"
                      >
                        Begin Verification
                        <ArrowRight className="w-5 h-5 ml-2 group-hover/button:translate-x-1 transition-transform" />
                      </Button>
                    </Link>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
} 