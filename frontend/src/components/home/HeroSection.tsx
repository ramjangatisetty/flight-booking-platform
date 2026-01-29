import { Box, Typography } from '@mui/material';
import FlightSearchWidget from './FlightSearchWidget';
import { testIds } from '../../testing/testIds';

interface HeroSectionProps {
  backgroundImage: string;
  title: string;
  subtitle: string;
}

export default function HeroSection({ backgroundImage, title, subtitle }: HeroSectionProps) {
  return (
    <Box
      data-testid={testIds.hero.root}
      sx={{
        position: 'relative',
        width: '100%',
        minHeight: { xs: '500px', md: '600px' },
        backgroundImage: `linear-gradient(rgba(11, 42, 74, 0.7), rgba(11, 42, 74, 0.5)), url(${backgroundImage})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        px: 2,
        py: 4,
      }}
    >
      <Typography
        variant="h2"
        sx={{
          color: 'white',
          fontWeight: 700,
          textAlign: 'center',
          mb: 2,
        }}
      >
        {title}
      </Typography>
      <Typography
        variant="h5"
        sx={{
          color: 'rgba(255, 255, 255, 0.9)',
          textAlign: 'center',
          mb: 4,
        }}
      >
        {subtitle}
      </Typography>
      <FlightSearchWidget />
    </Box>
  );
}
