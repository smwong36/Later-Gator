import React from 'react';
import { View, Text, Pressable } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { styles } from '../../styles';

const PomodoroScreen: React.FC = () => {
  const navigation = useNavigation<any>();

  return (
    <View style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.heading}>Pomodoro</Text>

        <View style={styles.card}>
          <Text style={styles.subheading}>25:00</Text>

          <Pressable style={styles.buttonAccent} onPress={() => { /* TODO: start timer */ }}>
            <Text style={styles.buttonText}>Start</Text>
          </Pressable>

          <Pressable style={styles.buttonSecondary} onPress={() => navigation.goBack()}>
            <Text style={styles.buttonText}>Back</Text>
          </Pressable>
        </View>
      </View>
    </View>
  );
};

export default PomodoroScreen;


// import React, { useEffect, useRef, useState } from 'react';
// import { View, Text, Button, StyleSheet, Vibration } from 'react-native';
// ///<reference types="node" />

// const WORK_MIN = 25;
// const BREAK_MIN = 5;

// const format = (s: number) => {
//   const mm = Math.floor(s / 60).toString().padStart(2, '0');
//   const ss = (s % 60).toString().padStart(2, '0');
//   return `${mm}:${ss}`;
// };

// export default function PomodoroScreen() {
    
// //   const [mode, setMode] = useState<'work' | 'break'>('work');
// //   const [seconds, setSeconds] = useState(WORK_MIN * 60);
// //   const [running, setRunning] = useState(false);
// //   const intervalRef = useRef<NodeJS.Timer | null>(null);

// //   // tick
// //   useEffect(() => {
// //     if (!running) return;
// //     intervalRef.current = setInterval(() => {
// //       setSeconds((prev) => {
// //         if (prev <= 1) {
// //           Vibration.vibrate(400);
// //           const nextMode = mode === 'work' ? 'break' : 'work';
// //           setMode(nextMode);
// //           return (nextMode === 'work' ? WORK_MIN : BREAK_MIN) * 60;
// //         }
// //         return prev - 1;
// //       });
// //     }, 1000);
// //     return () => {
// //       if (intervalRef.current) clearInterval(intervalRef.current);
// //     };
// //   }, [running, mode]);

// //   const onStartPause = () => setRunning((r) => !r);
// //   const onReset = () => {
// //     setRunning(false);
// //     setMode('work');
// //     setSeconds(WORK_MIN * 60);
//   };

//   return (
//     <View style={styles.container}>
//       <Text style={styles.title}>{mode === 'work' ? 'Focus' : 'Break'}</Text>
//       <Text style={styles.timer}>{format(seconds)}</Text>
//       <View style={styles.row}>
//         <Button title={running ? 'Pause' : 'Start'} onPress={onStartPause} />
//         <View style={{ width: 12 }} />
//         <Button title="Reset" onPress={onReset} />
//       </View>
//       <Text style={styles.hint}>25/5 default. Auto-switch on completion.</Text>
//     </View>
//   );
// }

// const styles = StyleSheet.create({
//   container: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 24 },
//   title: { fontSize: 28, fontWeight: '600', marginBottom: 8 },
//   timer: { fontSize: 64, fontVariant: ['tabular-nums'], marginVertical: 8 },
//   row: { flexDirection: 'row', marginTop: 16 },
//   hint: { marginTop: 12, opacity: 0.6 },
// });