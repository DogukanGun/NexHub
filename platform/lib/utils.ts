import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Merges multiple class names using clsx and tailwind-merge
 * Useful for combining Tailwind classes with dynamic classes
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
} 