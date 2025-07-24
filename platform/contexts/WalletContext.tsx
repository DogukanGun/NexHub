"use client"
import { createContext, useContext, useEffect, useState, ReactNode } from 'react';

interface WalletContextType {
  address: string | null;
  isConnecting: boolean;
  isConnected: boolean;
  connect: () => Promise<void>;
  disconnect: () => Promise<void>;
}

const WalletContext = createContext<WalletContextType>({
  address: null,
  isConnecting: false,
  isConnected: false,
  connect: async () => {},
  disconnect: async () => {},
});

/**
 * Provider component for Circle wallet integration
 * Manages wallet connection state and provides methods for connecting/disconnecting
 */
export function WalletProvider({ children }: { children: ReactNode }) {
  const [address, setAddress] = useState<string | null>(null);
  const [isConnecting, setIsConnecting] = useState(false);
  const [isConnected, setIsConnected] = useState(false);

  // Initialize Circle wallet
  useEffect(() => {
    // Circle wallet initialization will go here
    // This will be implemented when you handle the contract connection
  }, []);

  const connect = async () => {
    try {
      setIsConnecting(true);
      // Circle wallet connection logic will go here
      // For now, we'll just simulate a connection
      await new Promise(resolve => setTimeout(resolve, 1000));
      setAddress('0x...');
      setIsConnected(true);
    } catch (error) {
      console.error('Failed to connect wallet:', error);
    } finally {
      setIsConnecting(false);
    }
  };

  const disconnect = async () => {
    try {
      // Circle wallet disconnection logic will go here
      setAddress(null);
      setIsConnected(false);
    } catch (error) {
      console.error('Failed to disconnect wallet:', error);
    }
  };

  return (
    <WalletContext.Provider
      value={{
        address,
        isConnecting,
        isConnected,
        connect,
        disconnect,
      }}
    >
      {children}
    </WalletContext.Provider>
  );
}

/**
 * Hook to access wallet context
 * @returns WalletContextType
 */
export function useWallet() {
  const context = useContext(WalletContext);
  if (!context) {
    throw new Error('useWallet must be used within a WalletProvider');
  }
  return context;
} 