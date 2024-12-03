import discord
from discord import SelectOption, TextChannel, Interaction, app_commands
from discord.ui import Select, View
import logging
import os
import argparse
from typing import Optional
from dotenv import load_dotenv

def parse_arguments():
    parser = argparse.ArgumentParser(description='Discord Topic Channel Bot')
    parser.add_argument('--token', '-t',
                       default=os.environ.get('DISCORD_BOT_TOKEN'),
                       help='Discord bot token (can also be set via DISCORD_BOT_TOKEN environment variable)')
    parser.add_argument('--log-path', '-l',
                       default='/home/max/serious-gaming-bot/log/serious-gaming-bot.log',
                       help='Path to log file')
    args = parser.parse_args()
    
    if not args.token:
        parser.error("Token must be provided either via --token argument or DISCORD_BOT_TOKEN environment variable")
        
    return args

class TopicChannelSelect(Select):
    def __init__(self, channels: list[TextChannel]):
        # Create options from all channels in Topic Channels category
        options = [
            SelectOption(
                label=channel.name,
                value=str(channel.id),
                description=f"Show/hide {channel.name} channel"
            ) for channel in channels
        ]
        
        super().__init__(
            placeholder="Select channels to show...",
            min_values=0,
            max_values=len(options),
            options=options
        )

    async def callback(self, interaction: Interaction):
        await interaction.response.defer(ephemeral=True)
        member = interaction.user
        
        # Get all topic channels
        category = interaction.guild.get_channel(int(self.options[0].value)).category
        topic_channels = category.text_channels
        
        # Update permissions for all channels
        for channel in topic_channels:
            should_see = str(channel.id) in self.values
            await channel.set_permissions(member, view_channel=should_see)
        
        selected_channels = [channel.name for channel in topic_channels if str(channel.id) in self.values]
        if selected_channels:
            response = f"You can now see these channels: {', '.join(selected_channels)}"
        else:
            response = "You have hidden all topic channels"
            
        await interaction.followup.send(response, ephemeral=True)

class ChannelManagerView(View):
    def __init__(self, channels: list[TextChannel]):
        super().__init__(timeout=None)  # Make the view persistent
        self.add_item(TopicChannelSelect(channels))

class TopicChannelBot(discord.Client):
    def __init__(self):
        intents = discord.Intents.default()
        intents.message_content = True
        super().__init__(intents=intents)
        self.tree = app_commands.CommandTree(self)

    async def setup_hook(self):
        # Register the command for all guilds the bot is in
        for guild in self.guilds:
            self.tree.copy_global_to(guild=guild)
            await self.tree.sync(guild=guild)

    async def on_ready(self):
        logger.info(f'Bot is ready and logged in as {self.user}!')
        await self.setup_channel_manager()

    def get_topic_channels(self, guild: discord.Guild) -> Optional[list[TextChannel]]:
        """Get all channels in the Topic Channels category."""
        category = discord.utils.get(guild.categories, name="Topic Channels")
        if category:
            return category.text_channels
        return None

    async def setup_channel_manager(self):
        """Create or update the channel manager message in each guild."""
        for guild in self.guilds:
            # Find the setup channel
            setup_channel = discord.utils.get(guild.text_channels, name="topic-channels")
            if not setup_channel:
                logger.error(f"Could not find topic-channels channel in guild {guild.name}")
                continue

            topic_channels = self.get_topic_channels(guild)
            if not topic_channels:
                logger.error(f"Could not find Topic Channels category in guild {guild.name}")
                continue

            # Delete old messages
            try:
                async for message in setup_channel.history():
                    await message.delete()
            except discord.Forbidden:
                logger.error(f"Missing permissions to manage messages in {guild.name}")
                continue

            # Create new manager message
            try:
                view = ChannelManagerView(topic_channels)
                await setup_channel.send(
                    "**Topic Channel Manager**\nSelect which channels you want to see:",
                    view=view
                )
                logger.info(f"Successfully set up channel manager in {guild.name}")
            except discord.Forbidden:
                logger.error(f"Missing permissions to send messages in {guild.name}")

    @app_commands.command(name="refresh", description="Refresh the channel manager message")
    @app_commands.default_permissions(administrator=True)
    async def refresh(self, interaction: Interaction):
        """Command to refresh the channel manager message."""
        await interaction.response.defer(ephemeral=True)
        await self.setup_channel_manager()
        await interaction.followup.send("Channel manager has been refreshed!", ephemeral=True)

def main():
    # Load environment variables from .env file
    load_dotenv()
    
    # Parse command line arguments
    args = parse_arguments()
    
    # Setup logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler(filename=args.log_path, encoding='utf-8', mode='w'),
            logging.StreamHandler()
        ]
    )
    global logger
    logger = logging.getLogger('serious-gaming-bot')

    try:
        bot = TopicChannelBot()
        bot.run(args.token)
    except Exception as e:
        logger.critical(f'Failed to start bot: {str(e)}', exc_info=True)

if __name__ == "__main__":
    main()