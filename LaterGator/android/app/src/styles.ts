// styles.ts
import {StyleSheet} from 'react-native';

export const styles = StyleSheet.create({
  // Layout
  container: {
    flex: 1,
    backgroundColor: '#fcfbf7', // app background
  },
  content: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },

  // Text
  heading: {
    marginBottom: 30,
    fontSize: 24,
    fontWeight: '600',
    color: '#0021A5',
    textAlign: 'center',
  },
  subheading: {
    marginBottom: 16,
    fontSize: 16,
    fontWeight: '600',
    color: '#0021A5',
    textAlign: 'left',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#0B1B3C',
    marginBottom: 8,
  },
  bodyText: {
    fontSize: 14,
    color: '#0B1B3C',
  },

  // Cards/sections
  card: {
    width: '100%',
    backgroundColor: 'white',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 16,
    borderWidth: 1,
    borderColor: '#fcfbf7',
    marginBottom: 16,
  },
  section: {
    marginTop: 16,
    marginBottom: 8,
  },
  sectionBlue: {
    backgroundColor: '#e6f2ff',
    borderColor: '#cfe2ff',
    borderWidth: 1,
    borderRadius: 12,
    padding: 12,
    marginBottom: 12,
  },
  sectionOrange: {
    backgroundColor: '#fee8e2',
    borderColor: '#ffd9c8',
    borderWidth: 1,
    borderRadius: 12,
    padding: 12,
    marginBottom: 12,
  },

  // Login container
  loginContainer: {
    flex: 1,
    margin: 20,
    backgroundColor: '#e6f2ff',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 20,
  },

  // Inputs
  label: {
    fontSize: 12,
    color: '#64748B',
    marginBottom: 6,
  },
  textInput: {
    height: 35,
    width: '100%',
    paddingHorizontal: 10,
    backgroundColor: 'white',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    marginBottom: 12,
    fontSize: 12,
  },

  // Buttons
  button: {
    height: 40,
    width: '50%',
    backgroundColor: '#0021A5', // bold blue
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
    alignSelf: 'center',
  },
  buttonSecondary: {
    height: 40,
    width: '50%',
    backgroundColor: '#fdc0b0', // light orange
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
    alignSelf: 'center',
  },
  buttonAccent: {
    height: 40,
    width: '50%',
    backgroundColor: '#fb5a2f', // orange
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
    alignSelf: 'center',
  },
  buttonAccent2: {
    height: 40,
    width: '50%',
    backgroundColor: '#b3d7ff', // pastel blue
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
    alignSelf: 'center',
  },
  buttonText: {
    fontSize: 14,
    fontWeight: '600',
    color: 'white',
    textAlign: 'center',
  },

  // Horizontal separators
  hr: {
    height: 2,
    width: '100%',
    backgroundColor: '#E5E7EB',
    marginVertical: 12,
  },
  hrBold: {
    height: 4,
    width: '100%',
    backgroundColor: '#cfd8e3',
    marginVertical: 12,
  },
  separatorTop: {
    borderTopWidth: 1,
    borderTopColor: '#E5E7EB',
    paddingTop: 12,
    marginTop: 8,
  },


  // Rows/spacing
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  spacer: {
    height: 12,
    width: 12,
  },
  statRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 6,
  },
  sectionHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  
  toggleRowLeft: {
  flexDirection: 'row',
  alignItems: 'center',
  gap: 12,
  paddingVertical: 8,
},
toggleText: {
  fontSize: 16,
  color: '#0B1B3C',
},

// custom toggle parts (greyscale)
tglRing: {
  borderWidth: 2,
  borderRadius: 999,
  padding: 2,
},
tglRingOn: { borderColor: '#9CA3AF' },   // darker grey ring when ON
tglRingOff:{ borderColor: '#BDBDBD' },   // lighter grey ring when OFF

tglTrack: {
  width: 52,
  height: 28,
  borderRadius: 999,
  position: 'relative',
},
tglTrackOn:  { backgroundColor: '#F3F4F6' },
tglTrackOff: { backgroundColor: '#EEEEEE' },

tglKnob: {
  width: 20,
  height: 20,
  borderRadius: 999,
  position: 'absolute',
  top: 4,
},
tglKnobOn:  { backgroundColor: '#9CA3AF' },
tglKnobOff: { backgroundColor: '#BDBDBD' },

});

