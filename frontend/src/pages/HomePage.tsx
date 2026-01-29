import { Box } from '@mui/material';
import HeroSection from '../components/home/HeroSection';
import { heroImages } from '../assets/heroImages';
import { testIds } from '../testing/testIds';

export default function HomePage() {
  return (
    <Box data-testid={testIds.home.root}>
      <HeroSection
        backgroundImage={heroImages.home}
        title="Find Your Perfect Flight"
        subtitle="Book with confidence. Travel with ease."
      />
    </Box>
  );
}
