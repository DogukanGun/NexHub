'use client';
import { Navigation } from '@/components/layout/Navigation';
import { Button } from '@/components/ui/Button';
import { motion } from 'framer-motion';
import { Wallet, Info, AlertCircle } from 'lucide-react';
import { useState } from 'react';

interface BuyPageProps {
  params: {
    id: string;
  };
}

interface TokenInfo {
  name: string;
  symbol: string;
  price: string;
  minPurchase: string;
  maxPurchase: string;
  totalRaise: string;
  raisedAmount: string;
  tokenomics: {
    label: string;
    value: string;
  }[];
}

// This would typically come from an API
const tokenInfo: TokenInfo = {
  name: 'NexHub Token',
  symbol: 'NHT',
  price: '0.1 USDC',
  minPurchase: '100 USDC',
  maxPurchase: '10,000 USDC',
  totalRaise: '1,000,000 USDC',
  raisedAmount: '450,000 USDC',
  tokenomics: [
    { label: 'Initial Circulating Supply', value: '10,000,000 NHT' },
    { label: 'Total Supply', value: '100,000,000 NHT' },
    { label: 'Initial Market Cap', value: '$1,000,000' },
    { label: 'Vesting Period', value: '12 months' },
  ],
};

export default function BuyPage({ params }: BuyPageProps) {
  const [amount, setAmount] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);

  const handlePurchase = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!amount || isProcessing) return;

    setIsProcessing(true);
    try {
      // Here you would handle the actual purchase through Circle wallet
      await new Promise(resolve => setTimeout(resolve, 2000));
      // Handle success
    } catch (error) {
      console.error('Purchase failed:', error);
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <>
      <Navigation />
      <main className="min-h-screen pt-24 pb-12 px-4 bg-gradient-to-b from-white to-neutral-50">
        <div className="container mx-auto max-w-6xl">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-12"
          >
            <h1 className="text-4xl font-bold mb-4">Purchase Tokens</h1>
            <p className="text-xl text-neutral-600">
              You've been verified! Complete your token purchase below.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              className="space-y-6"
            >
              <div className="bg-white rounded-xl border border-neutral-200 p-6">
                <h2 className="text-2xl font-semibold mb-4">Token Information</h2>
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <span className="text-neutral-600">Token Name</span>
                    <span className="font-medium">{tokenInfo.name}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-neutral-600">Symbol</span>
                    <span className="font-medium">{tokenInfo.symbol}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-neutral-600">Price</span>
                    <span className="font-medium">{tokenInfo.price}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-neutral-600">Min Purchase</span>
                    <span className="font-medium">{tokenInfo.minPurchase}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-neutral-600">Max Purchase</span>
                    <span className="font-medium">{tokenInfo.maxPurchase}</span>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-xl border border-neutral-200 p-6">
                <h2 className="text-2xl font-semibold mb-4">Tokenomics</h2>
                <div className="space-y-4">
                  {tokenInfo.tokenomics.map((item, index) => (
                    <div key={index} className="flex justify-between items-center">
                      <span className="text-neutral-600">{item.label}</span>
                      <span className="font-medium">{item.value}</span>
                    </div>
                  ))}
                </div>
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              className="lg:sticky lg:top-24"
            >
              <div className="bg-white rounded-xl border border-neutral-200 p-6">
                <h2 className="text-2xl font-semibold mb-6">Purchase Tokens</h2>
                
                <div className="mb-6">
                  <div className="h-2 bg-neutral-100 rounded-full">
                    <div
                      className="h-2 bg-gradient-to-r from-purple-500 to-blue-500 rounded-full"
                      style={{
                        width: `${(parseInt(tokenInfo.raisedAmount.replace(/[^0-9]/g, '')) /
                          parseInt(tokenInfo.totalRaise.replace(/[^0-9]/g, ''))) *
                          100}%`,
                      }}
                    />
                  </div>
                  <div className="flex justify-between mt-2 text-sm text-neutral-600">
                    <span>Raised: {tokenInfo.raisedAmount}</span>
                    <span>Goal: {tokenInfo.totalRaise}</span>
                  </div>
                </div>

                <form onSubmit={handlePurchase} className="space-y-6">
                  <div>
                    <label className="block text-sm font-medium text-neutral-700 mb-2">
                      Amount (USDC)
                    </label>
                    <input
                      type="number"
                      value={amount}
                      onChange={e => setAmount(e.target.value)}
                      placeholder="Enter amount"
                      className="w-full rounded-lg border border-neutral-200 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-purple-500"
                    />
                    <p className="mt-2 text-sm text-neutral-500 flex items-center gap-1">
                      <Info className="w-4 h-4" />
                      You will receive: {amount ? `${Number(amount) * 10} NHT` : '0 NHT'}
                    </p>
                  </div>

                  <div className="p-4 bg-blue-50 rounded-lg flex items-start gap-3">
                    <AlertCircle className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
                    <p className="text-sm text-blue-700">
                      Make sure you have sufficient USDC in your Circle wallet before proceeding with the purchase.
                    </p>
                  </div>

                  <Button
                    type="submit"
                    variant="gradient"
                    size="lg"
                    className="w-full"
                    disabled={!amount || isProcessing}
                  >
                    <Wallet className="w-4 h-4 mr-2" />
                    {isProcessing ? 'Processing...' : 'Purchase Tokens'}
                  </Button>
                </form>
              </div>
            </motion.div>
          </div>
        </div>
      </main>
    </>
  );
} 