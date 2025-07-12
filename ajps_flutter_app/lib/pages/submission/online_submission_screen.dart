import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

class OnlineSubmissionScreen extends StatefulWidget {
  const OnlineSubmissionScreen({super.key});

  @override
  State<OnlineSubmissionScreen> createState() => _OnlineSubmissionScreenState();
}

class _OnlineSubmissionScreenState extends State<OnlineSubmissionScreen> {
  bool isLoading = false;

  void _showSnackBar(String message, {bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isError ? Colors.red : Colors.green,
      ),
    );
  }

  Future<void> _openSubmissionWebsite() async {
    setState(() => isLoading = true);

    try {
      final Uri submissionUrl = Uri.parse(
        'http://160.191.128.250:4500/user/submission/',
      );

      if (await canLaunchUrl(submissionUrl)) {
        await launchUrl(submissionUrl, mode: LaunchMode.externalApplication);
        _showSnackBar('Online submission website opened successfully.');
      } else {
        _showSnackBar('Could not open submission website', isError: true);
      }
    } catch (e) {
      _showSnackBar('Error opening submission website: $e', isError: true);
    } finally {
      setState(() => isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.web, size: 80, color: Colors.blue),
            const SizedBox(height: 24),
            const Text(
              'Online Submission',
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.bold,
                color: Colors.blue,
              ),
            ),
            const SizedBox(height: 12),
            const Text(
              'Submit your manuscript through our web-based system',
              style: TextStyle(fontSize: 16, color: Colors.grey),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 48),
            ElevatedButton.icon(
              onPressed: isLoading ? null : _openSubmissionWebsite,
              icon: isLoading
                  ? const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        color: Colors.white,
                      ),
                    )
                  : const Icon(Icons.open_in_browser),
              label: Text(isLoading ? 'Opening...' : 'Open Submission Website'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.blue,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(
                  horizontal: 32,
                  vertical: 16,
                ),
                textStyle: const TextStyle(fontSize: 18),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'http://160.191.128.250:4500/user/submission/',
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey[600],
                fontFamily: 'monospace',
              ),
            ),
          ],
        ),
      ),
    );
  }
}
